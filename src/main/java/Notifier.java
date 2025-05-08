import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.Calendar;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Notifier {
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final DbFunc dbFunc;
    private final ConvertBot bot;

    public Notifier(DbFunc dbFunc, ConvertBot bot){
        this.bot = bot;
        this.dbFunc = dbFunc;
    }
    public void startDailyNotifier(){
        long initDelay = calcInitDelay();
        scheduler.scheduleAtFixedRate(this::updateNotify,
                initDelay,
                24*60*60,
                TimeUnit.SECONDS);
    }
    private void updateNotify(){ // Потестить новый функционал и посмотреть, можно ли теперь добавлять валюты без подписки

        Map<Long, String> data = dbFunc.getUsersWithFavorite();
        if (data.isEmpty()) {

        }

        for(Map.Entry<Long, String> entry: data.entrySet()){

            SendMessage msg = new SendMessage();
            msg.setChatId(entry.getKey());

            if(!entry.getValue().isEmpty()) msg.setText("Ваши избранные валюты " + entry.getValue());

            try {
                bot.execute(msg);
            } catch (Exception e) {

                e.printStackTrace();
            }
        }

    }
    private long calcInitDelay(){
        Calendar now = Calendar.getInstance();
        Calendar nextRun = Calendar.getInstance();
        nextRun.set(Calendar.HOUR_OF_DAY, 12);
        nextRun.set(Calendar.MINUTE, 1);
        nextRun.set(Calendar.SECOND, 0);

        if (now.after(nextRun)) {
            nextRun.add(Calendar.DAY_OF_MONTH, 1);
        }

        return (nextRun.getTimeInMillis() - now.getTimeInMillis()) / 1000;
    }
    public void stop() {
        scheduler.shutdown();
    }
}
