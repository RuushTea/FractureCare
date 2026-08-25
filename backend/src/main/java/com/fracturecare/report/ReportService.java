package com.fracturecare.report;

import com.fracturecare.common.BadRequestException;
import com.fracturecare.common.NotFoundException;
import com.fracturecare.config.AppProperties;
import com.fracturecare.prediction.Prediction;
import com.fracturecare.prediction.PredictionService;
import com.fracturecare.prediction.PredictionStatus;
import jakarta.annotation.PostConstruct;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.math.RoundingMode;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ReportService {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM uuuu, HH:mm z")
            .withZone(ZoneId.systemDefault());
    private final ReportRepository reports;
    private final PredictionService predictionService;
    private final Path reportRoot;

    public ReportService(ReportRepository reports, PredictionService predictionService, AppProperties properties) {
        this.reports = reports;
        this.predictionService = predictionService;
        this.reportRoot = properties.storage().reports().toAbsolutePath().normalize();
    }

    @PostConstruct
    void initialize() throws IOException {
        Files.createDirectories(reportRoot);
    }

    @Transactional
    public ReportDtos.ReportResponse generate(Long userId, Long predictionId) {
        Prediction prediction = predictionService.requireOwned(userId, predictionId);
        if (prediction.getStatus() != PredictionStatus.COMPLETED) {
            throw new BadRequestException("A report can only be generated for a completed prediction.");
        }
        Report report = reports.findByPredictionId(predictionId).map(existing -> {
            writePdf(safeResolve(existing.getFileReference()), prediction);
            return existing;
        }).orElseGet(() -> createReport(prediction));
        return ReportDtos.ReportResponse.from(report);
    }

    @Transactional(readOnly = true)
    public ReportDownload download(Long userId, Long reportId) {
        Report report = reports.findByIdAndPredictionUserId(reportId, userId)
                .orElseThrow(() -> new NotFoundException("Report was not found."));
        try {
            Path path = safeResolve(report.getFileReference());
            Resource resource = new UrlResource(path.toUri());
            if (!resource.exists() || !resource.isReadable()) throw new NotFoundException("Report file is unavailable.");
            return new ReportDownload(resource, "fracturecare-report-" + report.getPrediction().getId() + ".pdf");
        } catch (IOException exception) {
            throw new NotFoundException("Report file is unavailable.");
        }
    }

    private Report createReport(Prediction prediction) {
        String reference = UUID.randomUUID() + ".pdf";
        Path path = safeResolve(reference);
        writePdf(path, prediction);
        return reports.save(new Report(prediction, reference));
    }

    private void writePdf(Path path, Prediction prediction) {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                float y = 790;
                y = writeLine(content, "FractureCare Prediction Report", 18, 50, y, true);
                y -= 10;
                y = writeLine(content, "Educational second-opinion support - not a medical diagnosis", 11, 50, y, true);
                y -= 16;
                y = writeLine(content, "Prediction ID: " + prediction.getId(), 11, 50, y, false);
                y = writeLine(content, "Created: " + DATE_FORMAT.format(prediction.getCreatedAt()), 11, 50, y, false);
                y = writeLine(content, "Image: " + prediction.getOriginalFileName(), 11, 50, y, false);
                y = writeLine(content, "Model version: " + prediction.getModelVersion(), 11, 50, y, false);
                y = writeLine(content, "Prediction class: " + friendly(prediction.getPredictedClass().name()), 11, 50, y, false);
                y = writeLine(content, "System-defined category: " + friendly(prediction.getRiskCategory().name()), 11, 50, y, false);
                y = writeLine(content, "Model confidence: " + prediction.getConfidence().multiply(java.math.BigDecimal.valueOf(100)).setScale(1, RoundingMode.HALF_UP) + "%", 11, 50, y, false);
                y -= 18;
                for (String line : wrap("This confidence reflects the model's certainty about its classification. It is not the probability of recovery and does not guarantee that the prediction is correct.", 88)) {
                    y = writeLine(content, line, 10, 50, y, false);
                }
                if (prediction.getExplanationSource() != null) {
                    y -= 12;
                    y = writeLine(content, "Plain-language explanation (" + friendly(prediction.getExplanationSource().name()) + ")", 11, 50, y, true);
                    for (String line : wrap(prediction.getExplanationSummary(), 88)) {
                        y = writeLine(content, line, 10, 50, y, false);
                    }
                    y -= 5;
                    for (String line : wrap("Confidence: " + prediction.getExplanationConfidenceMeaning(), 88)) {
                        y = writeLine(content, line, 10, 50, y, false);
                    }
                    y -= 5;
                    for (String line : wrap("Next step: " + prediction.getExplanationNextStep(), 88)) {
                        y = writeLine(content, line, 10, 50, y, false);
                    }
                }
                y -= 10;
                for (String line : wrap("Seek review from a qualified medical professional. Do not use this result to delay emergency care, make a diagnosis, or choose medication or treatment.", 88)) {
                    y = writeLine(content, line, 10, 50, y, false);
                }
                if (prediction.isSimulated()) {
                    y -= 12;
                    for (String line : wrap("SIMULATED RESULT: The AI model has not yet been connected. This report contains deterministic mock output for software testing only.", 88)) {
                        y = writeLine(content, line, 10, 50, y, true);
                    }
                }
            }
            document.save(path.toFile());
        } catch (IOException exception) {
            throw new IllegalStateException("Could not generate the prediction report", exception);
        }
    }

    private float writeLine(PDPageContentStream content, String text, float size, float x, float y, boolean bold) throws IOException {
        content.beginText();
        content.setFont(new PDType1Font(bold ? Standard14Fonts.FontName.HELVETICA_BOLD : Standard14Fonts.FontName.HELVETICA), size);
        content.newLineAtOffset(x, y);
        content.showText(text.replaceAll("[^\\x20-\\x7E]", "-"));
        content.endText();
        return y - (size + 5);
    }

    private List<String> wrap(String text, int width) {
        List<String> lines = new ArrayList<>();
        StringBuilder line = new StringBuilder();
        for (String word : text.split(" ")) {
            if (!line.isEmpty() && line.length() + word.length() + 1 > width) {
                lines.add(line.toString());
                line.setLength(0);
            }
            if (!line.isEmpty()) line.append(' ');
            line.append(word);
        }
        if (!line.isEmpty()) lines.add(line.toString());
        return lines;
    }

    private String friendly(String value) {
        String lower = value.toLowerCase().replace('_', ' ');
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }

    private Path safeResolve(String reference) {
        Path resolved = reportRoot.resolve(reference).normalize();
        if (!resolved.startsWith(reportRoot)) throw new BadRequestException("Invalid report reference.");
        return resolved;
    }

    public record ReportDownload(Resource resource, String fileName) {}
}
