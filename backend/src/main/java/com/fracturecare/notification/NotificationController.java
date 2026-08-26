package com.fracturecare.notification;
import com.fracturecare.security.CurrentUser;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationService service;
    public NotificationController(NotificationService service) { this.service = service; }
    @GetMapping public List<NotificationDtos.NotificationResponse> list(Authentication a) { return service.list(CurrentUser.from(a).id()); }
    @GetMapping("/unread-count") public NotificationDtos.UnreadCount unread(Authentication a) { return new NotificationDtos.UnreadCount(service.unreadCount(CurrentUser.from(a).id())); }
    @PatchMapping("/{id}/read") public NotificationDtos.NotificationResponse read(@PathVariable Long id, Authentication a) { return service.markRead(CurrentUser.from(a).id(), id); }
}
