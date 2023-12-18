package project.midterm.midtermandroid.Model;

public class User {
    private String name;
    private String email;
    private String password;
    private long age;
    private String phone;
    private boolean status;
    private String role;
    private String photo;
    public User() {
        // Default constructor required for Firestore
    }
    public User(String name, String email, String password, String role) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public User(String name, String email, String password, long age, String phone, boolean status, String role, String photo) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.age = age;
        this.phone = phone;
        this.status = status;
        this.role = role;
        this.photo = photo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public long getAge() {
        return age;
    }

    public void setAge(long age) {
        this.age = age;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public boolean getStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    //    private String encryptPassword(String plainTextPassword) {
//        try {
//            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
//            keyStore.load(null);
//
//            KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
//            KeyGenParameterSpec keyGenParameterSpec = new KeyGenParameterSpec.Builder(
//                    email,
//                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
//                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
//                    .setUserAuthenticationRequired(false)
//                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
//                    .build();
//
//            keyGenerator.init(keyGenParameterSpec);
//            SecretKey secretKey = keyGenerator.generateKey();
//
//            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
//            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
//
//            byte[] encryptedBytes = cipher.doFinal(plainTextPassword.getBytes());
//            return Base64.encodeToString(encryptedBytes, Base64.DEFAULT);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
//    }
//
//    public boolean verifyPassword(String plainTextPassword) {
//        try {
//            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
//            keyStore.load(null);
//
//            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
//            KeyStore.SecretKeyEntry secretKeyEntry = (KeyStore.SecretKeyEntry) keyStore.getEntry(email, null);
//            SecretKey secretKey = secretKeyEntry.getSecretKey();
//
//            cipher.init(Cipher.DECRYPT_MODE, secretKey);
//            byte[] decryptedBytes = cipher.doFinal(Base64.decode(password, Base64.DEFAULT));
//            String decryptedPassword = new String(decryptedBytes);
//
//            return decryptedPassword.equals(plainTextPassword);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return false;
//        }
//    }
}
