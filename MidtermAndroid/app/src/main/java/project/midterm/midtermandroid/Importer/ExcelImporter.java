package project.midterm.midtermandroid.Importer;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import project.midterm.midtermandroid.Model.Certificate;
import project.midterm.midtermandroid.Model.Student;

public class ExcelImporter {

    public static ArrayList<Student> importStudentsFromExcel(InputStream inputStream) {
        ArrayList<Student> students = new ArrayList<>();

        try {
            Workbook workbook = WorkbookFactory.create(inputStream);
            Sheet sheet = workbook.getSheetAt(0); // Assuming data is in the first sheet

            for (Row row : sheet) {
                // Assuming data starts from the second row (index 1), change as needed
                if (row.getRowNum() > 0) {
                    String studentID = getStudentIDCellValue(row.getCell(0));
                    String fullName = getStringCellValue(row.getCell(1));
                    String birthdate = formatDate(getDateCellValue(row.getCell(2))); // Format date
                    String gender = getStringCellValue(row.getCell(3));
                    String phone = getStringCellValue(row.getCell(4));
                    String email = getStringCellValue(row.getCell(5));
                    String address = getStringCellValue(row.getCell(6));
                    String gpa = getStringCellValue(row.getCell(7)); // Change to string for Firebase

                    // Create Student object and add to list
                    Student student = new Student(studentID, fullName, birthdate, gender, phone, email, address, Double.valueOf(gpa));
                    students.add(student);
                }
            }

            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return students;
    }

    public static void exportStudentsToExcel(Activity activity, List<Student> students, String filePath) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Students");

        // Create headers
        String[] headers = {"Student ID", "Full Name", "Birthdate", "Gender", "Phone", "Email", "Address", "GPA"};
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }

        // Fill in student data
        int rowNum = 1;
        for (Student student : students) {
            Row row = sheet.createRow(rowNum++);

            row.createCell(0).setCellValue(student.getStudentID());
            row.createCell(1).setCellValue(student.getFullname());
            row.createCell(2).setCellValue(student.getBirthdate());
            row.createCell(3).setCellValue(student.getGender());
            row.createCell(4).setCellValue(student.getPhone());
            row.createCell(5).setCellValue(student.getEmail());
            row.createCell(6).setCellValue(student.getAddress());
            row.createCell(7).setCellValue(student.getGpa());
        }

        // Write to file
        try {
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "students.xlsx");
            FileOutputStream fileOut = new FileOutputStream(file);
            workbook.write(fileOut);
            fileOut.close();

            Toast.makeText(activity, "File saved to Downloads", Toast.LENGTH_SHORT).show();

            // Notify the system about the saved file
            MediaScannerConnection.scanFile(activity, new String[]{file.getAbsolutePath()}, null,
                    (path, uri) -> {
                        Intent openFileIntent = new Intent(Intent.ACTION_VIEW);
                        openFileIntent.setDataAndType(uri, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                        openFileIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        activity.startActivity(Intent.createChooser(openFileIntent, "Open file with"));
                    });
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                workbook.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static ArrayList<Certificate> importCertificatesFromExcel(InputStream inputStream) {
        ArrayList<Certificate> certificates = new ArrayList<>();

        try {
            Workbook workbook = WorkbookFactory.create(inputStream);
            Sheet sheet = workbook.getSheetAt(0); // Assuming data is in the first sheet

            for (Row row : sheet) {
                // Assuming data starts from the second row (index 1), change as needed
                if (row.getRowNum() > 0) {
                    String cerName = getStringCellValue(row.getCell(0));
                    String cerDate = formatDate(getDateCellValue(row.getCell(1)));
                    String cerDescription = getStringCellValue(row.getCell(2));
                    String cerSchool = getStringCellValue(row.getCell(3));

                    // Create Certificate object and add to list
                    Certificate certificate = new Certificate(cerName, cerDate, cerDescription, cerSchool);
                    certificates.add(certificate);
                }
            }

            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return certificates;
    }

    public static void exportCertificatesToExcel(Activity activity, List<Certificate> certificates, String filePath) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Certificates");

        // Create headers
        String[] headers = {"Name", "Date", "Description", "School"};
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }

        // Fill in certificate data
        int rowNum = 1;
        for (Certificate certificate : certificates) {
            Row row = sheet.createRow(rowNum++);

            row.createCell(0).setCellValue(certificate.getName());
            row.createCell(1).setCellValue(certificate.getDate());
            row.createCell(2).setCellValue(certificate.getDescription());
            row.createCell(3).setCellValue(certificate.getSchool());
        }

        // Write to file
        try {
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "certificates.xlsx");
            FileOutputStream fileOut = new FileOutputStream(file);
            workbook.write(fileOut);
            fileOut.close();

            Toast.makeText(activity, "File saved to Downloads", Toast.LENGTH_SHORT).show();

            // Notify the system about the saved file
            MediaScannerConnection.scanFile(activity, new String[]{file.getAbsolutePath()}, null,
                    (path, uri) -> {
                        Intent openFileIntent = new Intent(Intent.ACTION_VIEW);
                        openFileIntent.setDataAndType(uri, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                        openFileIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        activity.startActivity(Intent.createChooser(openFileIntent, "Open file with"));
                    });
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                workbook.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static String getStringCellValue(Cell cell) {
        if (cell != null) {
            switch (cell.getCellType()) {
                case STRING:
                    return cell.getStringCellValue();
                case NUMERIC:
                    if (DateUtil.isCellDateFormatted(cell)) {
                        return formatDate(cell.getDateCellValue()); // Format date
                    } else {
                        return String.valueOf(cell.getNumericCellValue());
                    }
                case BOOLEAN:
                    return String.valueOf(cell.getBooleanCellValue());
                case FORMULA:
                    return cell.getCellFormula();
                default:
                    return ""; // Return empty string for other types
            }
        } else {
            return ""; // Return empty string for null cells
        }
    }

    private static String getStudentIDCellValue(Cell cell) {
        if (cell != null) {
            if (cell.getCellType() == CellType.STRING) {
                return cell.getStringCellValue();
            } else if (cell.getCellType() == CellType.NUMERIC) {
                // Ensure student ID is treated as a string even if it's a numeric cell
                return String.valueOf((long) cell.getNumericCellValue());
            } else {
                // Handle other cell types if needed
                return ""; // Return empty string for other types
            }
        } else {
            return ""; // Return empty string for null cells
        }
    }

    private static Date getDateCellValue(Cell cell) {
        if (cell != null && cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return cell.getDateCellValue();
        }
        return null;
    }

    private static String formatDate(Date date) {
        if (date != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            return dateFormat.format(date);
        }
        return "";
    }
}