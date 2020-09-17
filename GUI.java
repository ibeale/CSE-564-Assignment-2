import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.util.concurrent.TimeUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.JLabel;

// Adapted from https://stackoverflow.com/questions/5801734/how-to-draw-lines-in-java
public class GUI extends JComponent{

    private ArrayList<Line> lines = new ArrayList<Line>();
    public FileReader fr = new FileReader();
    public TSPSolver ts;
    public boolean isStarted = false;
    public JPanel buttonsPanel = new JPanel();
    public JButton clearButton = new JButton("Clear");
    public JButton toggleRunButton = new JButton("Start");
    public JButton importButton = new JButton("Import");
    public JLabel currentFileLabel = new JLabel("Current File: None");
    public JTextField filePathInput = new JTextField("Enter file path here.", 25);

    public GUI(){
        buttonsPanel.add(clearButton);
        buttonsPanel.add(toggleRunButton);
        buttonsPanel.add(importButton);
        buttonsPanel.add(filePathInput);
        buttonsPanel.add(currentFileLabel);
        toggleRunButton.setEnabled(false);
        filePathInput.addMouseListener(new MouseAdapter(){
            @Override
            public void mouseClicked(MouseEvent e){
                filePathInput.setText("");
            }
        });

        importButton.addActionListener(new importButtonAction(this));

        clearButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                clearLines();
            }
        });
        toggleRunButton.addActionListener(new runButtonAction(this));

    }

    private static class Line{
        final int x1; 
        final int y1;
        final int x2;
        final int y2;   

        public Line(int x1, int y1, int x2, int y2) {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
        }               
    }



    public void addLine(int x1, int x2, int x3, int x4) {
        lines.add(new Line(x1,x2,x3,x4));        
        paintImmediately(0,0,800,800);
    }

    public void clearLines() {
        lines.clear();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        for (Line line : lines) {
            g.drawLine(line.x1, line.y1, line.x2, line.y2);
        }
    }

    public static void main(String[] args) {
        JFrame mainWindow = new JFrame();
        mainWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        final GUI comp = new GUI();
        comp.setPreferredSize(new Dimension(800, 800));
        mainWindow.getContentPane().add(comp, BorderLayout.CENTER);
        mainWindow.getContentPane().add(comp.buttonsPanel, BorderLayout.SOUTH);
        mainWindow.pack();
        mainWindow.setVisible(true);
    }

}

class BackgroundDecorator extends SwingWorker<Void, Void> {

    private GUI comp;

    public BackgroundDecorator(GUI comp){
        this.comp = comp;
    }

    @Override
    public Void doInBackground() {
        if(comp.isStarted == false){
            comp.isStarted = true;
            double scaleFactor;
            double xScale = ((TSPSolver.maxX - TSPSolver.minX)/800);
            double yScale = ((TSPSolver.maxY - TSPSolver.minY)/800);

            scaleFactor = (xScale > yScale) ? xScale : yScale;
            int i;
            for(i = 1; i < TSPSolver.pathTaken.size() + 1; i++){
                if( i == TSPSolver.pathTaken.size()){
                    double city_x1 = TSPSolver.pathTaken.get(i-1).x;
                    double city_x2 = TSPSolver.pathTaken.get(0).x;
                    double city_y1 = TSPSolver.pathTaken.get(i-1).y;
                    double city_y2 = TSPSolver.pathTaken.get(0).y;
                    int x1 = (int)Math.round((city_x1 - TSPSolver.minX) / scaleFactor);
                    int x2 = (int)Math.round((city_x2 - TSPSolver.minX) / scaleFactor); 
                    int y1 = (int)Math.round((city_y1 - TSPSolver.minY) / scaleFactor); 
                    int y2 = (int)Math.round((city_y2 - TSPSolver.minY) / scaleFactor);
                    System.out.println(x1 + " " + y1 + " "+ x2+" "+ y2);
                    comp.addLine(x1,y1,x2,y2);
                }
                double city_x1 = TSPSolver.pathTaken.get(i-1).x;
                double city_x2 = TSPSolver.pathTaken.get(i).x;
                double city_y1 = TSPSolver.pathTaken.get(i-1).y;
                double city_y2 = TSPSolver.pathTaken.get(i).y;
                int x1 = (int)Math.round((city_x1 - TSPSolver.minX) / scaleFactor);
                int x2 = (int)Math.round((city_x2 - TSPSolver.minX) / scaleFactor); 
                int y1 = (int)Math.round((city_y1 - TSPSolver.minY) / scaleFactor); 
                int y2 = (int)Math.round((city_y2 - TSPSolver.minY) / scaleFactor);
                comp.addLine(x1,y1,x2,y2);
                try{
                    Thread.sleep(1000);
                }catch(InterruptedException ie){
                    System.out.print(ie.getStackTrace());
                }
            }
        }
        comp.isStarted = false;
        return null;
    }

}

class BackgroundSolver extends SwingWorker<Void, Void>{
    
    GUI comp;
    String fileName;

    public BackgroundSolver(GUI comp, String fileName){
        this.comp = comp;
        this.fileName = fileName;
    }

    @Override
    public Void doInBackground() {
        comp.importButton.setEnabled(false);
        if(comp.ts.calcTSP()){
            comp.currentFileLabel.setText(String.format("Solve complete. Current File: %s", fileName));
            comp.toggleRunButton.setEnabled(true);
        }
        else{
            comp.currentFileLabel.setText("Error Solving TSP.");
            comp.toggleRunButton.setEnabled(false);
        }
        comp.importButton.setEnabled(true);
        return null;
    }
}

class importButtonAction implements ActionListener{

    private GUI comp;

    public importButtonAction(GUI comp){
        this.comp = comp;
    }

    public void actionPerformed(ActionEvent e){
        String fp = comp.filePathInput.getText();
        String[] pathArr = fp.split("\\\\");
        int lastIdx = pathArr.length - 1;
        boolean isRead = comp.fr.readFile(fp);
        String fileName = pathArr[lastIdx];
        if(isRead){
            comp.ts = new TSPSolver(comp.fr.cities);
            comp.currentFileLabel.setText("Solving... Please wait.");
            BackgroundSolver bs = new BackgroundSolver(comp, fileName);
            bs.execute();
        }
        else{
            comp.currentFileLabel.setText("ERROR: Invalid file path given.");
            comp.toggleRunButton.setEnabled(false);
        }
    }
}

class runButtonAction implements ActionListener{
    BackgroundDecorator dec;
    JLabel currentFileLabel;

    public runButtonAction(GUI comp){
        this.dec = new BackgroundDecorator(comp);
    }

    public void actionPerformed(ActionEvent e){
        dec.execute();
    }
}

class City{
    public int cityID;
    public double x;
    public double y;

    public City(int cityID, double x, double y){
        this.cityID = cityID;
        this.x = x;
        this.y = y;
    }

    public String toString(){
        return "CityID: " + this.cityID + " x: " + this.x + " y: " + this.y;
    }
}

class TSPSolver{
    public static ArrayList<City> pathTaken = new ArrayList<City>();
    public ArrayList<City> cities;
    public double totalDistance = 0;
    public static double maxX = 0;
    public static double maxY = 0;
    public static double minX = 999999999;
    public static double minY = 999999999;

    public TSPSolver(ArrayList<City> cities){
        this.cities = cities;
    }

    public void showPath(){
        if(pathTaken.size() < cities.size()){
            System.out.println("Path has not been created");
        }
        else{
            for(City c : pathTaken){
                System.out.println(c.cityID);
            }
            System.out.println(totalDistance);
        }
        
    }
    
    public double calcDistance(City c1, City c2){
        return Math.sqrt(Math.pow(c2.x - c1.x, 2) + Math.pow(c2.y - c1.y, 2));
    }

    public boolean calcTSP(){
        totalDistance = 0;
        City curCity = cities.get(0);
        City closestCity = null;
        int numCities = cities.size();
        int[] visited = new int[numCities];
        double minDist;
        double distance = 0;
        Arrays.fill(visited, 0);
        visited[curCity.cityID] = 1;
        pathTaken.add(curCity);
        maxX = curCity.x;
        maxY = curCity.y;
        minX = curCity.x;
        minY = curCity.y;

        while(pathTaken.size() != numCities){
            minDist = 999999999;
            for(City c : cities){
                distance = calcDistance(curCity, c);
                if(c.cityID != curCity.cityID && visited[c.cityID] != 1 && distance < minDist){
                    closestCity = c;
                    minDist = distance;
                }
            }
            curCity = closestCity;
            if(curCity.x > maxX){
                maxX = curCity.x;
            }
            if(curCity.y > maxY){
                maxY = curCity.y;
            }
            if(curCity.x < minX){
                minX = curCity.x;
            }
            if(curCity.y < minY){
                minY = curCity.y;
            }
            totalDistance += minDist;
            visited[curCity.cityID] = 1;
            pathTaken.add(curCity);
            
        }
        if(pathTaken.size() == cities.size()){
            return true;
        }
        else{
            return false;
        }
        
    }
}

class FileReader{
    public int dimensions;
    public ArrayList<City> cities = new ArrayList<City>();

    public boolean isNumeric(String num){
        if (num == null){
            return false;
        }
        try{
            double d = Double.parseDouble(num);
        }catch(NumberFormatException e){
            return false;
        }
        return true;
    }

    public boolean readFile(String filePath){
        try{
            cities.clear();
            File fileObj = new File(filePath);
            Scanner fileReader = new Scanner(fileObj);
            while (fileReader.hasNextLine()){
                String data = fileReader.nextLine();
                String[] lineArr = data.split(" ");
                if(data.indexOf("DIMENSION") != -1){
                    int lastIdx = lineArr.length - 1;
                    dimensions = Integer.parseInt(lineArr[lastIdx]);
                }
                else if(isNumeric(lineArr[0])){
                    int cityID = Integer.parseInt(lineArr[0]) - 1;
                    double x = Double.parseDouble(lineArr[1]);
                    double y = Double.parseDouble(lineArr[2]);
                    City newCity = new City(cityID, x, y);
                    this.cities.add(newCity);
                }
            }
            fileReader.close();
            return true;
        } catch(FileNotFoundException e){
            return false;
        }
    }
}
