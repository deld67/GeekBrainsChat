package ru.geekbrains.java2.client.model;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class FileLogService {
    private static final String PathToDir = new File("./log").getAbsolutePath();
    private static String fileName;
    private static Path filePath;
    private static final int maxLinesRead = 100;

    public void InitLog(String fileName){
        try {
            if (!Files.exists( Paths.get( PathToDir ))) {
                Path directory = Files.createDirectory( Paths.get( PathToDir ) );
            }
            if (!Files.exists(Paths.get(PathToDir+"/"+ fileName  )  )){
                Files.createFile( Paths.get(PathToDir+"/"+ fileName ));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.filePath = Paths.get(PathToDir+"/"+ fileName);
    }

    public  void writeMessageToLog(String message){
        try {
            Files.writeString( filePath, message, StandardOpenOption.APPEND );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<String> readMessagesFromLog() throws IOException {
        return  Files.readAllLines( filePath );
    }

    public int getMaxLinesRead() {
        return maxLinesRead;
    }
}
