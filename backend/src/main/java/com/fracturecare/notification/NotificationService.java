package com.fracturecare.notification;
import com.fracturecare.common.NotFoundException;
import com.fracturecare.prediction.Prediction;
import com.fracturecare.user.UserAccount;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
@Service
public class NotificationService {
    private final NotificationRepository notifications;
    public NotificationService(NotificationRepository notifications) { this.notifications = notifications; }
    @Transactional public Notification createReviewCompleted(UserAccount user, Prediction prediction) { return notifications.save(new Notification(user, prediction)); }
    @Transactional(readOnly = true) public List<NotificationDtos.NotificationResponse> list(Long userId) { return notifications.findByUserIdOrderByCreatedAtDesc(userId).stream().map(NotificationDtos.NotificationResponse::from).toList(); }
    @Transactional(readOnly = true) public long unreadCount(Long userId) { return notifications.countByUserIdAndReadFalse(userId); }
    @Transactional public NotificationDtos.NotificationResponse markRead(Long userId, Long id) { Notification n = notifications.findByIdAndUserId(id, userId).orElseThrow(() -> new NotFoundException("Notification was not found.")); n.markRead(); return NotificationDtos.NotificationResponse.from(notifications.save(n)); }
}
