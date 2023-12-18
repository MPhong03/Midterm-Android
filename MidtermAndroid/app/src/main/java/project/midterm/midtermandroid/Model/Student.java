package project.midterm.midtermandroid.Model;

public class Student {
    private String studentID;
    private String fullname;
    private String birthdate;
    private String gender;
    private String phone;
    private String email;
    private String address;
    private double gpa;

    public Student() {
    }

    public Student(String studentID, String fullname, String birthdate, String gender, String phone, String email, String address, double gpa) {
        this.studentID = studentID;
        this.fullname = fullname;
        this.birthdate = birthdate;
        this.gender = gender;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.gpa = gpa;
    }

    public String getStudentID() {
        return studentID;
    }

    public void setStudentID(String studentID) {
        this.studentID = studentID;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(String birthdate) {
        this.birthdate = birthdate;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public double getGpa() {
        return gpa;
    }

    public void setGpa(double gpa) {
        this.gpa = gpa;
    }
}
