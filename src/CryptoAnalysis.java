import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;
import static java.nio.file.StandardOpenOption.APPEND;

public class CryptoAnalysis {

    private static final String ALPABETRUSSIAN = "АаБбВвГгДдЕеЁёЖжЗзИиЙйКкЛлМмНнОоПпРрСсТтУуФфХхЦцЧчШшЩщЪъЫыЬьЭэЮюЯя";
    private static final String ALPABETENGLISH = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String PUNCTUATIONMARKS = ".,\"-! ?';:()*+";
    private static final String NUMBERS = "0123456789";
    private static final String currentAlphabet = ALPABETENGLISH + ALPABETRUSSIAN + PUNCTUATIONMARKS + NUMBERS;
    private static final int sizeAlphabet = currentAlphabet.length();
    private static Path workinPath;
    private static List<String> fileStrings = new ArrayList<>();
    private static int cryptoKey;

    public static void main(String[] args) {

        int operatingMode = selectOperatingMode();

        if (operatingMode == 0) {
            System.out.println("Exceeded the number of attempts. Please restart the program");
        } else if (operatingMode == 1) {
            encrypt();
        } else if (operatingMode == 2) {
            decrypt();
        } else if (operatingMode == 3) {
            bruteForseDecrypt();
        } else if (operatingMode == 4) {
            decryptByAnalysis();
        } else if (operatingMode == 5) {
            System.out.println("GoodBye. See you later !");
        }
    }
    public static void encrypt() {
        System.out.println("Let's do the encoding !");
        if (!getCryptoKey()) {return;}
        if (!getStringsfromFile()) {return;}
        List<String>encryptStrings = getCryptStrings(fileStrings, cryptoKey);
        putStringsToFile(encryptStrings);
    }

    public static void decrypt() {
        System.out.println("Let's do the decoding!");
        if (!getCryptoKey()) {return;}
        if (!getStringsfromFile()) {return;}
        List<String> decryptStrings = getDeCryptStrings(fileStrings, cryptoKey);
        putStringsToFile(decryptStrings);
    }

    public static void bruteForseDecrypt() {
        System.out.println("Let's try to find a secret key!");
        if (!getStringsfromFile()) {return;}
        List<String> decryptStrings = getBruteForseDeCryptStrings(fileStrings);
        putStringsToFile(decryptStrings);
    }

    public static void decryptByAnalysis() {
        System.out.println("Let's do cryptanalysis!  Need a sample to collect statistics !");
        if (!getStringsfromFile()) {return;}
        Map<Character, Double> mapAnalyticsExample = getMapAnalytics(fileStrings);
        System.out.println("Now decoding! Give me your encoded file!");
        if (!getStringsfromFile()) {return;}
        Map<Character, Double> mapAnalyticsCrypto = getMapAnalytics(fileStrings);
        Map<Character, Character> decodingMap = getDecodingMap(mapAnalyticsExample, mapAnalyticsCrypto);
        List<String> encryptStrings = getDecodeAnalyticsStrings(fileStrings, decodingMap);
        putStringsToFile(encryptStrings);
    }


    private static void putStringsToFile(List<String> encryptStrings) {

        String localDateString = LocalTime.now().getHour() + "-" + LocalTime.now().getMinute() +
                "-" + LocalTime.now().getSecond();
        String fileName = localDateString + "_result_" + workinPath.getFileName().toString();
        Path currentPath = workinPath.getParent().resolve(fileName);
        Path file;
        try {
            file = Files.createFile(currentPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        for (String encryptString : encryptStrings) {
            try {
                Files.writeString(file, encryptString, APPEND);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        System.out.println("You will find the result of the operation in " + currentPath);
    }

    private static boolean getCryptoKey() {
        boolean result = false;
        Scanner scanner = new Scanner(System.in);
        System.out.println("Give me your secret key...");
        int count = 0;
        while (count < 3) {
            if (scanner.hasNextInt()) {
                cryptoKey = scanner.nextInt();
                if (cryptoKey < 0) {
                    System.out.println("Enter a number greater than zero");
                } else {
                    result = true;
                    break;
                }
            } else {
                System.out.println("Enter a number greater than zero");
                scanner.nextLine();
            }
            count++;
        }
        if (!result) {
            System.out.println("Exceeded the number of attempts.Invalid key value");
        }
        return result;
    }

    private static boolean getStringsfromFile() {
        System.out.println("Enter the path to the file");
        Scanner scanner = new Scanner(System.in);
        workinPath = Path.of(scanner.nextLine());

        if (Files.isRegularFile(workinPath)) {
            try {
                fileStrings = Files.readAllLines(workinPath);
                if (fileStrings.size() == 0) {
                    System.out.println("Empty file");
                    return false;
                } else { return true;}

            } catch (IOException e) {
                System.out.println("File reading error...");
                throw new RuntimeException(e);
            }
        } else {
            System.out.println("Incorrect path or file format");
            return false;
        }
    }
    private static List<String> getDeCryptStrings(List<String> fileStrings, int localCryptoKey) {
        List<String> resultList = new ArrayList<>();

        for (String fileString : fileStrings) {
            char[] stringChars = fileString.toCharArray();
            for (int i = 0; i < stringChars.length; i++) {

                int charIndex = currentAlphabet.indexOf(Character.toString(stringChars[i]));
                if (localCryptoKey <= (charIndex)) {
                    stringChars[i] = currentAlphabet.charAt(charIndex - localCryptoKey);
                } else {
                    int keyFromTail = localCryptoKey - charIndex;
                    if (keyFromTail < sizeAlphabet) {

                        stringChars[i] = currentAlphabet.charAt(sizeAlphabet - keyFromTail);
                    } else {
                        keyFromTail = keyFromTail % sizeAlphabet;
                        stringChars[i] = currentAlphabet.charAt(sizeAlphabet - keyFromTail);
                    }
                }
            }
            resultList.add(String.copyValueOf(stringChars));
        }
        return resultList;
    }

    private static List<String> getCryptStrings(List<String> fileStrings, int keyCrypto) {
        List<String> resultList = new ArrayList<>();


        for (String fileString : fileStrings) {
            char[] stringChars = fileString.toCharArray();
            for (int i = 0; i < stringChars.length; i++) {
                int charIndex = currentAlphabet.indexOf(Character.toString(stringChars[i]));
                if (charIndex < 0) {
                    charIndex = currentAlphabet.indexOf(" ");
                }
                if (keyCrypto < (sizeAlphabet - charIndex)) {
                    stringChars[i] = currentAlphabet.charAt(charIndex + keyCrypto);
                } else {
                    int keyFromZero = keyCrypto - (sizeAlphabet - charIndex);
                    if (keyFromZero < sizeAlphabet) {
                        stringChars[i] = currentAlphabet.charAt(keyFromZero);
                    } else {
                        keyFromZero = keyFromZero % sizeAlphabet;
                        stringChars[i] = currentAlphabet.charAt(keyFromZero);
                    }
                }
            }
            resultList.add(String.copyValueOf(stringChars));
        }
        System.out.println("The encoding was successful !");
        return resultList;
    }

    private static int selectOperatingMode() {
        int result = 0;
        int countAttems = 0;
        Scanner scanner = new Scanner(System.in);
        System.out.println("Select operating mode:");
        System.out.println("1. " + "Encrypt");
        System.out.println("2. " + "Decrypt");
        System.out.println("3. " + "Brute Force");
        System.out.println("4. " + "Statistyc Method");
        System.out.println("5. Exit");

        while (countAttems < 3) {
            if (scanner.hasNextInt()) {
                result = scanner.nextInt();
                if (result == 1) {
                    return result;
                } else if (result == 2) {
                    return result;
                } else if (result == 3) {
                    return result;
                } else if (result == 4) {
                    return result;
                } else if (result == 5) {
                    return result;
                } else {
                    System.out.println("Enter a number from 1 to 5");
                }
            } else {
                System.out.println("Enter a number from  1 to 5");
                scanner.nextLine();
            }
            countAttems++;
        }
        return result;
    }


    private static List<String> getDecodeAnalyticsStrings(List<String> fileStringsCrypto, Map<Character, Character> decodingMap) {
        List<String> resultList = new ArrayList<>();

        for (String fileString : fileStringsCrypto) {
            char[] stringChars = fileString.toCharArray();
            for (int i = 0; i < stringChars.length; i++) {
                stringChars[i] = decodingMap.get(stringChars[i]);
            }
            resultList.add(String.copyValueOf(stringChars));
        }
        return resultList;
    }

    private static List<String> getBruteForseDeCryptStrings(List<String> fileStrings) {
        List<String> resultList = new ArrayList<>();
        for (int i = 0; i < sizeAlphabet - 1; i++) {
            resultList = getDeCryptStrings(fileStrings, i);
            if (checkBruteForse(resultList)) {
                System.out.println("Ключ расшифровки " + i);
                return resultList;
            }
        }
        return resultList;
    }

    private static boolean checkBruteForse(List<String> resultList) {
        int comma = 0;
        int dot = 0;
        for (String checkString : resultList) {
            comma = checkString.indexOf(", ");
            dot = checkString.indexOf(". ");
        }
        return (comma > 0) && (dot > 0);
    }
    private static Map<Character, Character> getDecodingMap(Map<Character, Double> mapAnalyticsExample, Map<Character, Double> mapAnalyticsCrypto) {
        Map<Character, Character> decodingMap = new HashMap<>();
        char removeChar = ' ';

        for (Map.Entry<Character, Double> exampleEntry : mapAnalyticsExample.entrySet()) {

            for (Map.Entry<Character, Double> cryptoEntry : mapAnalyticsCrypto.entrySet()) {
                decodingMap.put(cryptoEntry.getKey(), exampleEntry.getKey());
                removeChar = cryptoEntry.getKey();
                break;
            }
            mapAnalyticsCrypto.remove(removeChar);
        }
        return decodingMap;
    }


    private static Map<Character, Double> getMapAnalytics(List<String> fileStrings) {
        Map<Character, Double> mapAnalysis = new HashMap<>();
        fileStrings = getCryptStrings(fileStrings, 0);
        int lenghtText;

        for (String fileString : fileStrings) {
            lenghtText = fileString.length();
            char[] stringChars = fileString.toCharArray();
            for (int al = 0; al < sizeAlphabet; al++) {
                char symbolAlphabet = currentAlphabet.charAt(al);
                int count = 0;

                for (int i = 0; i < stringChars.length; i++) {
                    if (stringChars[i] == symbolAlphabet) {
                        count++;
                    }
                }
                mapAnalysis.put(symbolAlphabet, (double) count / (double) lenghtText * 100);
            }
        }

        Map<Character, Double> sortedMap = mapAnalysis.entrySet().stream()
                .sorted(Comparator.comparingDouble(e -> -e.getValue()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> {
                            throw new AssertionError();
                        },
                        LinkedHashMap::new
                ));
        return sortedMap;
    }
}