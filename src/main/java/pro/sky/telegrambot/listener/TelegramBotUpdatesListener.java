package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.model.Notification;
import pro.sky.telegrambot.repository.NotificationRepository;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    private Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);
    private final NotificationRepository notificationRepository;

    public TelegramBotUpdatesListener(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Autowired
    private TelegramBot telegramBot;

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {
        updates.forEach(update -> {
            logger.info("Processing update: {}", update);
            if (update.message().text().equals("/start")) {
                long chatId = update.message().chat().id();
                SendMessage message = new SendMessage(chatId, "hi!");
                SendResponse response = telegramBot.execute(message);
            }
            Pattern pattern = Pattern.compile("([0-9 .:\\s]{16})(\\s)([\\W+]+)");
            Matcher matcher = pattern.matcher(update.message().text());
            if (matcher.matches()) {
                long chatId = update.message().chat().id();
                SendMessage message = new SendMessage(chatId, add(matcher, chatId).toString());
                SendResponse response = telegramBot.execute(message);
            }
        });
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    private Notification add(Matcher matcher, long chatId) {
        String date = matcher.group(1);
        String item = matcher.group(3);
        LocalDateTime time = LocalDateTime.parse(date, DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
        return notificationRepository.save(new Notification(chatId, item, time));
    }

    @Scheduled(cron = "*/5 * * * * *")
    public void checkTime() {
        LocalDateTime time = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        List<LocalDateTime> data = notificationRepository.findAllTime();
        System.out.println("data = " + data);
        System.out.println("time = " + time);
        for (LocalDateTime datum : data) {
            if (time.isAfter(datum)) {
                notificationRepository.delete(notificationRepository.findByTime(datum));
            } else if (time.isEqual(datum)) {
                Notification notification = notificationRepository.findByTime(datum);
                SendMessage message = new SendMessage(notification.getChatId(), notification.getNotification());
                SendResponse response = telegramBot.execute(message);
            }
        }
    }
}
//   */1 * * * * *
//   0 0/1 * * * *