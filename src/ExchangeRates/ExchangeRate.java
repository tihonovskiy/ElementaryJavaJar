package ExchangeRates;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Semaphore;

public class ExchangeRate {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        DateTimeFormatter f = DateTimeFormatter.ofPattern("dd.MM.yyyy");

        HashMap<String, Integer> allCurrency = new HashMap();
        allCurrency.put(AllCurrency.USD.name(), AllCurrency.USD.getI());
        allCurrency.put(AllCurrency.AUD.name(), AllCurrency.AUD.getI());
        allCurrency.put(AllCurrency.CZK.name(), AllCurrency.CZK.getI());

        System.out.println("Enter currency code: ");
        String userCurrencyCode = sc.nextLine();
        System.out.println("Enter start date: (format = dd.mm.yyyy, example 01.01.2022)");
        LocalDate startLocalDate = LocalDate.parse(sc.nextLine(), f);
        System.out.println("Enter end date: (format = dd.mm.yyyy, example 01.01.2022)");
        LocalDate endLocalDate = LocalDate.parse(sc.nextLine(), f);

        do {
            if (allCurrency.containsKey(userCurrencyCode) && endLocalDate.compareTo(startLocalDate) > 0) {
                Semaphore semaphore = new Semaphore(3);

                try {
                    semaphore.acquire();
                    new Thread(getExchangeRateRunner(allCurrency, userCurrencyCode, startLocalDate, endLocalDate)).start();
                    getCurrencyInfo(allCurrency, userCurrencyCode, startLocalDate, endLocalDate);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    semaphore.release();
                }

                break;

            } else {
                System.out.println("Currency code or date entered incorrectly - try again: ");
                userCurrencyCode = sc.nextLine();
                System.out.println("Enter start date: (format = dd.mm.yyyy, example 01.01.2022)");
                startLocalDate = LocalDate.parse(sc.nextLine(), f);
                System.out.println("Enter end date: (format = dd.mm.yyyy, example 01.01.2022)");
                endLocalDate = LocalDate.parse(sc.nextLine(), f);
            }
        } while (true);

    }

    public static void getCurrencyInfo(HashMap<String, Integer> allCurrency, String userCurrencyCode, LocalDate startLocalDate, LocalDate endLocalDate) {
        String fullCurrencyName;
        int count = endLocalDate.compareTo(startLocalDate) + 1;
        List<String> info = new ArrayList<>();
        List<Double> priceAllDays = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            File file = new File("./ExchangeRates/" + startLocalDate);

            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    info.add(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            priceAllDays.add(Double.parseDouble(info.get(allCurrency.get(userCurrencyCode)).split("[|]")[7]));
            startLocalDate = startLocalDate.plusDays(1);
        }

        fullCurrencyName = info.get(allCurrency.get(userCurrencyCode)).split("[|]")[6];

        System.out.printf("UAH [Ukrainian Hryvnia] к %s [%s] -> %f", userCurrencyCode, fullCurrencyName, getAverage(priceAllDays));
    }

    public static double getAverage(List<Double> priceAllDays) {
        double result = 0;

        for (int i = 0; i < priceAllDays.size(); i++) {
            result += priceAllDays.get(0);
        }

        return result / priceAllDays.size();
    }

    static Runnable getExchangeRateRunner(HashMap<String, Integer> allCurrency, String userCurrencyCode, LocalDate startLocalDate, LocalDate endLocalDate) {
        return new Runnable() {
            @Override
            public void run() {
                getCurrencyInfo(allCurrency, userCurrencyCode, startLocalDate, endLocalDate);
            }
        };
    }

}
