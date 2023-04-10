package src;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static java.lang.Math.abs;


public class Caesar {
    private static Scanner scanner = new Scanner(System.in);
    private static final ArrayList<Character> ALPHABET = new ArrayList<>( //"алфавит" шифра
            List.of(
                    'а', 'б', 'в', 'г', 'д', 'е', 'ё', 'ж', 'з', 'и', 'й', 'к', 'л', 'м', 'н',
                    'о', 'п', 'р', 'с', 'т', 'у', 'ф', 'х', 'ц', 'ч', 'ш', 'щ', 'ъ', 'ы', 'ь',
                    'э', 'ю', 'я', '.', ',', '"', ':', '-', '!', '?', ' ' // size = 41
            ));
    private static final ArrayList<Character> MARKS = new ArrayList<>( //символы после которых обычно ставится пробел
            List.of(
                    '.', ',', ':', '!', '?'
            ));

    public static void main(String[] args) {
        System.out.println("Welcome to the Caesar cipher broker");

        int idMethod = methodChoosing(scanner); //Выбор режима программы

        String inputFilePath = fileName(scanner); //получение пути до файла

        try {

            encryptionDecryption(idMethod, inputFilePath);

        } catch (NoSuchElementException e) {
            System.out.println("The file might be empty");
        }
        scanner.close();
    }

    /**
     * Выбор режима программы
     */
    private static int methodChoosing(Scanner scanner) {
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

    /**
     * Установка пути до файла
     */
    private static String fileName(Scanner scanner) {
        String path = null;
        do {
            System.out.println("Enter the absolute path of file (.txt):");

            while (!scanner.hasNext()) { //если в сканер ввели неподходящий формат
                System.out.println("You entered the wrong path. Try again.");
            }

            path = scanner.nextLine();

            if (Files.notExists(Path.of(path)) || !Path.of(path).isAbsolute()) //проверка пути на существование файла и что путь является абсолютным
                System.out.println("Wrong path of file. Try again");

        } while (Files.notExists(Path.of(path)) || !Path.of(path).isAbsolute() || path.equals(""));
        return path;
    }

    /**
     * создание пути для нового файла
     */
    private static String outputFilePath(String oldPath) {
        int dotIndex = oldPath.lastIndexOf(".");
        return oldPath.substring(0, dotIndex) + "Encrypted" + oldPath.substring(dotIndex);
    }

    /**
     * Метод в котором происходит открытие файла и создание нового
     * и основная работа программы в зависимости от выбора режима программы
     */
    private static void encryptionDecryption(int idMethod, String filePath) throws NoSuchElementException {
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

                    while (((character = reader.read()) != -1)) { //добавляем в лист весь файл
                        charList.add((char) character);
                    }

                    int shift = bruteForce(charList);

                    System.out.println("The key is: " + shift);

                    for (int i = 0; i < charList.size(); i++) {
                        char origChar = charList.get(i);
                        char encryptedChar = encrypt(origChar, shift);
                        charList.set(i, encryptedChar);

                        if (i == 0)
                            encryptedChar = Character.toUpperCase(encryptedChar); //устанавливаем верхний регистр для первой буквы в тексте
                        else if (i > 1 && charList.get(i - 1).equals('\n')) //устанавливаем верхний регистр для буквы после переноса строки
                            encryptedChar = Character.toUpperCase(encryptedChar);
                        else if (i > 2 && (charList.get(i - 2).equals('.') || charList.get(i - 2).equals('!') || charList.get(i - 2).equals('?'))) //устанавливаем верхний регистр для буквы после точки
                            encryptedChar = Character.toUpperCase(encryptedChar);

                        writer.write(encryptedChar);
                    }

                }
            }
        } catch (IOException e) {
            System.out.println("Oh.. Smth went wrong :(" + e);
        }
    }

    /**
     * Установка значения смещения и его проверка
     */
    private static int shiftValue(Scanner scanner) {
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

        } while (shift == 0 || (abs(shift) > ALPHABET.size() - 1)); //повторение пока сдвиг не подходит под условие
        return shift;
    }

    /**
     * Метод шифрования/расшифрования с известным сдвигом
     */
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

    /**
     * Автоматический метод расшифроки грубой силой
     */
    private static int bruteForce(ArrayList<Character> charList) {
        HashMap<Integer, Integer> shiftCandidates = new HashMap<>(); // мапа в которой будет хранится возможный сдвиг и количество его входов

        for (int i = 0; i < ALPHABET.size(); i++) { //проверка при каждом сдвиге
            ArrayList<Character> encList = new ArrayList<>();

            for (char orig : charList) {
                encList.add(encrypt(orig, i)); //изменение текста при проверяемом сдвиге
            }

            int counter = 0; //счётчик для количества совпадений
            for (int j = 0; j < encList.size() - 1; j++) { //размер текста
                if (MARKS.contains(encList.get(j)) && (encList.get(j + 1).equals(' ') || encList.get(j + 1).equals('\n'))) //проверка на наличие пробела или переноса строки после знака препинания
                    counter++; //прибавляем случай в счётчик
                if (MARKS.contains(encList.get(j)) && (ALPHABET.indexOf(encList.get(j + 1)) <= 32)) //если после знака препинания, идёт буква, но не должна
                    counter--; //отнимаем значение из счётчика
            }

            if (counter != 0)
                shiftCandidates.put(counter, i); //добавляем случай в мапу
        }
        System.out.println(shiftCandidates);
        int maxValue = Collections.max(shiftCandidates.keySet()); //максимальное значение входов
        return shiftCandidates.get(maxValue);
    }
}

