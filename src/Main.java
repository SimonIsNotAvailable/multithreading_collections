import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    private static BlockingQueue<String> list1 = new ArrayBlockingQueue<>(100);
    private static BlockingQueue<String> list2 = new ArrayBlockingQueue<>(100);
    private static BlockingQueue<String> list3 = new ArrayBlockingQueue<>(100);
    private static AtomicInteger counterA = new AtomicInteger(0);
    private static AtomicInteger counterB = new AtomicInteger(0);
    private static AtomicInteger counterC = new AtomicInteger(0);

    public static void main(String[] args) {

        int length = 100_000;    // length of the String
        int quantity = 10_000;   // quantity of the generated strings

        ExecutorService service = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        service.submit(() -> {
            String[] texts = new String[quantity];
            for (int i = 0; i < texts.length; i++) {
                texts[i] = generateText("abc", length);
                try {
                    list1.put(texts[i]);
                    list2.put(texts[i]);
                    list3.put(texts[i]);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        service.submit(() -> {
            for (int i = 0; i < quantity; i++) {
                try {
                    countSymbol(list1.take(), "a");
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        service.submit(() -> {
            String temp;
            for (int i = 0; i < quantity; i++) {
                try {
                    temp = list2.take();
                    countSymbol(temp, "b");
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        service.submit(() -> {
            for (int i = 0; i < quantity; i++) {
                try {
                    countSymbol(list3.take(), "c");
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        service.shutdown();

        try {
            service.awaitTermination(20, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(counterA + " символов а \n" +
                    counterB + " символов b \n" +
                    counterC + " символов а \n");
    }

    private static void countSymbol(String text, String symbol) {
        int count = text.length() - text.replace(symbol, "").length();
        switch (symbol) {
            case "a":
                counterA.getAndAdd(count);
                break;
            case "b":
                counterB.getAndAdd(count);
                break;
            case "c":
                counterC.getAndAdd(count);
                break;
        }
    }

    public static String generateText(String letters, int length) {
        Random random = new Random();
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < length; i++) {
            text.append(letters.charAt(random.nextInt(letters.length())));
        }
        return text.toString();
    }
}
