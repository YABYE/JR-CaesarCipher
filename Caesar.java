import java.io.*;
import java.nio.file.*;
import java.util.*;


import static java.lang.Math.abs;

public class Caesar {
    private static final ArrayList<Character> ALPHABET = new ArrayList<>( //"алфавит" шифра
            List.of(
                    'а', 'б', 'в', 'г', 'д', 'е', 'ё', 'ж', 'з', 'и', 'й', 'к', 'л', 'м', 'н',
                    'о', 'п', 'р', 'с', 'т', 'у', 'ф', 'х', 'ц', 'ч', 'ш', 'щ', 'ъ', 'ы', 'ь',
                    'э', 'ю', 'я', '.', ',', '\"', ':', '-', '!', '?', ' ' // size = 41
            ));

    private static Scanner scanner = new Scanner(System.in);


    public static void main(String[] args) {
        System.out.println("Welcome to the Caesar cipher broker");
        int idMethod = methodChoosing(scanner); //Выбор режима программы
        String inputFilePath = fileName(scanner); //получение пути до файла
        encryptionDecryption(idMethod, inputFilePath);
        scanner.close();
    }

    private static int methodChoosing(Scanner scanner) { //Выбор режима программы
        System.out.println("To encrypt or decrypt the file with shift value, enter \"1\"\nTo decrypt the file with brute force, enter \"2\":");
        int idMethod;
        do {
            while (!scanner.hasNextInt()) {
                System.out.println("It's not an integer number, try again.");
                scanner.next();
            }
            idMethod = scanner.nextInt();
            if (idMethod != 1 && idMethod != 2) {
                System.out.println("You entered an incorrect number. Try again.");
            }
        } while (idMethod != 1 && idMethod != 2);
        scanner.nextLine();
        return idMethod;
    }

    private static String fileName(Scanner scanner) {  //Установка пути до файла
        String path = null;
        do {
            System.out.println("Enter the absolute path of file:");
            while (!scanner.hasNext()) { //если в сканер ввели неподходящий формат
                System.out.println("You entered the wrong path. Try again.");
            }
            path = scanner.nextLine();
            if (Files.notExists(Path.of(path)) || !Path.of(path).isAbsolute()) //проверка пути на существование файла и что путь является абсолютным
                System.out.println("Wrong path of file. Try again");
        } while (Files.notExists(Path.of(path)) || !Path.of(path).isAbsolute() || path.equals(""));
        return path;
    }

    private static String outputFilePath(String oldPath) { //Создание пути для нового файла
        int dotIndex = oldPath.lastIndexOf(".");
        return oldPath.substring(0, dotIndex) + "Changed" + oldPath.substring(dotIndex);
    }

    private static void encryptionDecryption(int idMethod, String filePath) { //работа с файлами
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath(filePath)))) {
            switch (idMethod) {
                case 1 -> { //режим при известном сдвиге
                    int shift = shiftValue(scanner);
                    int character;
                    while ((character = reader.read()) != -1) {
                        char originalChar = (char) character;
                        char encryptedChar = encrypt(originalChar, shift);
                        writer.write(encryptedChar);
                    }
                }
                case 2 -> { //режим при неизвестном сдвиге
                    ArrayList<Character> charList = new ArrayList<>();
                    int character;
                    while (((character = reader.read()) != -1)) {
                        charList.add((char) character);
                    }
                    int shift = cryptValue(charList);
                    for (char characterList : charList) {
                        char encryptedChar = encrypt(characterList, shift);
                        writer.write(encryptedChar);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Oh.. Smth went wrong :(" + e);
        }
    }


    private static int shiftValue(Scanner scanner) { //Установка значения смещения и его проверка
        int shift; //объявление переменной сдвига
        do {
            System.out.println("Enter the shift value:");
            while (!scanner.hasNextInt()) { //если в сканере не целочисленная переменная
                System.out.println("It's not an integer number, try again.");
                scanner.next(); //Очищаем сканер
            }
            shift = scanner.nextInt(); //установка значения сдвига
            if (shift == 0) {
                System.out.println("Shift value can't be a 0, it has no meaning. Try again:");
            } else if (abs(shift) > ALPHABET.size() - 1) {
                System.out.println("You entered a number that bigger than size of Alphabet. Try again:");
            }
        } while (shift == 0 || (abs(shift) > ALPHABET.size() - 1));
        return shift;
    }

    private static char encrypt(char orig, int shift) {
        char encrypted = Character.toLowerCase(orig);   // Привидение буквы к нижнему регистру
        if (!ALPHABET.contains(encrypted))  // Если символа нет в алфавите, то его не нужно шифровать
            return orig;
        int index = (ALPHABET.indexOf(encrypted) + shift) % ALPHABET.size();    // индекс расшифрованной буквы
        if (index < 0)
            index += ALPHABET.size();
        encrypted = ALPHABET.get(index); // расшифрованная буква
        return Character.isUpperCase(orig) ? Character.toUpperCase(encrypted) : Character.toLowerCase(encrypted);   // Возвращаем зашифрованный символ в оригинальном регистре
    }

    private static int cryptValue(ArrayList<Character> charList) { //метод для определения сдвига путём нахождения пробела
        HashMap<Character, Integer> candidates = new HashMap<>(); //мапа для кандидатов на роль пробела
        int counterInt = 0; //счётчик входа возможного пробела
        for (int i = 2; i < charList.size(); i++) {
            char candidate = ' '; //возможный кандидат на роль пробела
            if (Character.isUpperCase(charList.get(i)) && ALPHABET.contains(Character.toLowerCase(charList.get(i - 1))) && !candidates.containsKey(charList.get(i-1))) { //ищем букву в верхнем регистре, проверяем, что она есть в алфавите шифра и проверяем на отсутствие в мапе
                candidate = charList.get(i - 1); //на роль кандидата идёт символ стоящий перед символом из проверки
                for (int j = i; j < charList.size(); j++) {
                    if (candidate == charList.get(j)) //ищем все вхождения этого символа
                        counterInt++;
                }
                candidates.put(candidate, counterInt); //добавляем в мапу "кандидата" и количество входов
            }
        }
        Character maxKey = null;
        for (char key : candidates.keySet()) {
            if (maxKey == null || candidates.get(key) > candidates.get(maxKey)) {
                maxKey = key; //получаем из мапы символ с максимальным количеством вхождений
            }
        }
        return ALPHABET.indexOf(' ') - ALPHABET.indexOf(maxKey); //получение сдвига
    }
}

