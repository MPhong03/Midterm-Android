package project.midterm.midtermandroid.Model;

public class Certificate {
    private String name;
    private String date;
    private String description;
    private String school;

    public Certificate() {
    }

    public Certificate(String name, String date, String description, String school) {
        this.name = name;
        this.date = date;
        this.description = description;
        this.school = school;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSchool() {
        return school;
    }

    public void setSchool(String school) {
        this.school = school;
    }
}
