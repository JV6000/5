import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class PasswordManager {
    private static final String FILE_NAME = "passwords.csv";
    private static final String ENCRYPTED_FILE_NAME = "passwords.enc";
    private static String AES_KEY;

    public static void main(String[] args) throws Exception {
        AES_KEY = AESUtil.getKey();

        File encryptedFile = new File(ENCRYPTED_FILE_NAME);
        if (encryptedFile.exists()) {
            decryptFile();
        } else {
            new File(FILE_NAME).createNewFile();
        }

        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("1. Add Password");
            System.out.println("2. Search Password");
            System.out.println("3. Update Password");
            System.out.println("4. Delete Password");
            System.out.println("5. Exit");
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    addPassword(scanner);
                    break;
                case 2:
                    searchPassword(scanner);
                    break;
                case 3:
                    updatePassword(scanner);
                    break;
                case 4:
                    deletePassword(scanner);
                    break;
                case 5:
                    encryptFile();
                    System.exit(0);
            }
        }
    }

    private static void addPassword(Scanner scanner) throws Exception {
        System.out.print("Enter name: ");
        String name = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();
        System.out.print("Enter URL/application: ");
        String url = scanner.nextLine();
        System.out.print("Enter comment: ");
        String comment = scanner.nextLine();

        String encryptedPassword = AESUtil.encrypt(password, AES_KEY);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME, true))) {
            writer.write(String.join(",", name, encryptedPassword, url, comment));
            writer.newLine();
        }
    }

    private static void searchPassword(Scanner scanner) throws Exception {
        System.out.print("Enter name: ");
        String name = scanner.nextLine();
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts[0].equals(name)) {
                    String decryptedPassword = AESUtil.decrypt(parts[1], AES_KEY);
                    System.out.println("Name: " + parts[0]);
                    System.out.println("Password: " + decryptedPassword);
                    System.out.println("URL/Application: " + parts[2]);
                    System.out.println("Comment: " + parts[3]);
                    return;
                }
            }
        }
        System.out.println("Password not found.");
    }

    private static void updatePassword(Scanner scanner) throws Exception {
        System.out.print("Enter name: ");
        String name = scanner.nextLine();
        System.out.print("Enter new password: ");
        String newPassword = scanner.nextLine();

        File tempFile = new File("temp.csv");
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME));
             BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
            String line;
            boolean found = false;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts[0].equals(name)) {
                    parts[1] = AESUtil.encrypt(newPassword, AES_KEY);
                    found = true;
                }
                writer.write(String.join(",", parts));
                writer.newLine();
            }
            if (!found) {
                System.out.println("Password not found.");
            }
        }

        Path source = tempFile.toPath();
        Path target = Paths.get(FILE_NAME);
        Files.move(source, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
    }

    private static void deletePassword(Scanner scanner) throws Exception {
        System.out.print("Enter name: ");
        String name = scanner.nextLine();

        File tempFile = new File("temp.csv");
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME));
             BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
            String line;
            boolean found = false;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (!parts[0].equals(name)) {
                    writer.write(String.join(",", parts));
                    writer.newLine();
                } else {
                    found = true;
                }
            }
            if (!found) {
                System.out.println("Password not found.");
            }
        }

        Path source = tempFile.toPath();
        Path target = Paths.get(FILE_NAME);
        Files.move(source, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
    }

    private static void encryptFile() throws Exception {
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME));
             BufferedWriter writer = new BufferedWriter(new FileWriter(ENCRYPTED_FILE_NAME))) {
            String line;
            while ((line = reader.readLine()) != null) {
                writer.write(AESUtil.encrypt(line, AES_KEY));
                writer.newLine();
            }
        }
        new File(FILE_NAME).delete();
    }

    private static void decryptFile() throws Exception {
        try (BufferedReader reader = new BufferedReader(new FileReader(ENCRYPTED_FILE_NAME));
             BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME))) {
            String line;
            while ((line = reader.readLine()) != null) {
                writer.write(AESUtil.decrypt(line, AES_KEY));
                writer.newLine();
            }
        }
    }
}
