package work.SecondProcess;

import work.SecondProcess.Form.FormApplication;
import work.record.Record;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static java.lang.Thread.sleep;

public class Main {

    public static ConcurrentMap<String,Record> fileContent = new ConcurrentHashMap<>();
    public static ArrayList<String> idInDB = new ArrayList<>();

    public static void main(String[] args) {
        Reading reading = new Reading("гадим_сюда.json");
        try {
            DataBase db = new DataBase();
            try {
                while (!reading.isOver) {
                    RandomAccessFile file = new RandomAccessFile(reading.getPath(),"rw");
                    FileChannel fileChannel = file.getChannel();
                    FileLock lock = fileChannel.lock();
                    Record record = reading.read(file);
                    lock.release();
                    fileChannel.close();
                    file.close();
                    if(record != null){
                        if(!idInDB.contains(record.getId())){
                            db.createRecord(record);}
                        fileContent.put(record.getId(),record);
                    } else if(!reading.isOver) {
                        try {
                            sleep(5000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (SQLException sqlEx) {
            sqlEx.printStackTrace();
        } finally {
            try { DataBase.con.close(); } catch(SQLException se) { /*can't do anything */ }
            try { DataBase.stmt.close(); } catch(SQLException se) { /*can't do anything */ }
            try { DataBase.rs.close(); } catch(SQLException se) { /*can't do anything */ }
        }
        FormApplication.main(args);
    }
}
