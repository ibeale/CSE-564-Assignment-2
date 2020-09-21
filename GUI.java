import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.Timer;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.JLabel;


//Line class represents a line by using 4 points.
class Line{
    public int x1; 
    public int y1;
    public int x2;
    public int y2;   

    public Line(int x1, int y1, int x2, int y2) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }               
}

// Adapted from https://stackoverflow.com/questions/5801734/how-to-draw-lines-in-java
public class GUI extends JComponent{

    private ArrayList<Line> lines = new ArrayList<Line>();
    public ArrayList<Point> points = new ArrayList<Point>();
    public FileReader fr = new FileReader();
    public TSPSolver ts;
    public boolean isStarted = false;
    public JPanel buttonsPanel = new JPanel();
    public JButton toggleRunButton = new JButton("Start");
    public JButton importButton = new JButton("Import");
    public JLabel currentFileLabel = new JLabel("Current File: None");
    public JTextField filePathInput = new JTextField("Enter file path here.", 25);
    public JLabel totalDistance = new JLabel("Total Distance Travelled: 0, Iteration: 0");
    public JTextField startingIndex = new JTextField("Starting idx", 10);
    public double distanceTravelled = 0;
    public int iteration = 0;
    public Timer timer = new Timer(1000, new ActionListener(){
        @Override
        public void actionPerformed(ActionEvent e){
            totalDistance.setText(String.format("Total Distance Travelled: %.2f, Iteration: %d", distanceTravelled, iteration));
        }
    });

    /**
     * GUI Constructor assembles all of the Jcomponents to be ready to be displayed
     * also adds actionListeners to the buttons.
     */
    public GUI(){
        buttonsPanel.add(toggleRunButton);
        buttonsPanel.add(importButton);
        buttonsPanel.add(filePathInput);
        buttonsPanel.add(startingIndex);
        buttonsPanel.add(currentFileLabel);
        buttonsPanel.add(totalDistance);
        toggleRunButton.setEnabled(false);
        filePathInput.addMouseListener(new MouseAdapter(){
            @Override
            public void mouseClicked(MouseEvent e){
                filePathInput.setText("");
            }
        });

        importButton.addActionListener(new importButtonAction(this));
        toggleRunButton.addActionListener(new runButtonAction(this));

    }


    /**
     * Takes two coordinates and creates a line object and adds it to the GUI's collection of lines.
     * Also invokes paint immediately so that it doesn't wait for the parent function to return
     * before displaying all of the lines.
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     */
    public void addLine(int x1, int y1, int x2, int y2) {
        lines.add(new Line(x1,y1,x2,y2));        
        paintImmediately(0,0,800,800);
    }


    public void clearPoints() {
        points.clear();
        repaint();
    }


    /**
     * Clears the GUI's lines and repaints, thus clearing the screen.
     */
    public void clearLines() {
        lines.clear();
        repaint();
    }

    //Invoked by repaint and paintImmediately, overidden to display all lines.
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        for(Point p : points){
            g.drawOval(p.x, p.y, 2, 2);
        }
        for (Line line : lines) {
            g.drawLine(line.x1, line.y1, line.x2, line.y2);
        }
    }

    //Main function to assemble the GUI and make things visible.
    public static void main(String[] args) {
        JFrame mainWindow = new JFrame();
        mainWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        final GUI comp = new GUI();
        comp.setPreferredSize(new Dimension(800, 800));
        mainWindow.getContentPane().add(comp, BorderLayout.CENTER);
        mainWindow.getContentPane().add(comp.totalDistance, BorderLayout.NORTH);
        mainWindow.getContentPane().add(comp.buttonsPanel, BorderLayout.SOUTH);
        mainWindow.pack();
        mainWindow.setVisible(true);
    }

}

//Background decorator class which draws the lines in the background so the GUI buttons continue to work.
class BackgroundDecorator extends SwingWorker<Void, Void> {

    private GUI comp;
    public boolean finished = false;
    public int i;

    public BackgroundDecorator(GUI comp){
        this.comp = comp;
    }

    //Function is called by SwingWorker.execute(). Describes what will happen in the background.
    @Override
    public Void doInBackground() {
        if(comp.isStarted == true){
            comp.distanceTravelled = 0;
            //The timer which will update the main GUI with the path length and iteration.
            comp.timer.start();
            for(i = 1; i < comp.ts.pathTaken.size() + 1; i++){
                comp.iteration = i;
                if(comp.isStarted == false){
                    // Stop the GUI timer so that it doesn't get out of sync of this thread.
                    comp.timer.stop();
                    // Continue drawing each line unless the stop button is pressed
                    // The while loop blocks this thread until it is restarted.
                    while(!comp.isStarted){
                        try{
                            // Blocking without a sleep causes issues.
                            Thread.sleep(1000);
                        }catch(InterruptedException ie){
                            ;
                        }
                    }
                    comp.timer.start();
                }
                // Special case to handle the last iteration
                if( i == comp.ts.pathTaken.size()){
                    comp.distanceTravelled += comp.ts.calcDistance(comp.ts.pathTaken.get(i-1), comp.ts.pathTaken.get(0));
                    Point p1 = comp.points.get(i-1);
                    Point p2 = comp.points.get(0);
                    comp.addLine(p1.x,p1.y,p2.x,p2.y);
                }
                // Turn the cities into scaled points to fit inside of the frame and add it to the GUI's line arraylist.
                else{
                    comp.distanceTravelled += comp.ts.calcDistance(comp.ts.pathTaken.get(i-1), comp.ts.pathTaken.get(i));
                    Point p1 = comp.points.get(i-1);
                    Point p2 = comp.points.get(i);
                    comp.addLine(p1.x,p1.y,p2.x,p2.y);
                }
                try{
                    Thread.sleep(1000);
                }catch(InterruptedException ie){
                    System.out.print(ie.getStackTrace());
                }
            }
            finished = true;
            comp.isStarted = false;
            comp.timer.stop();
        }
        comp.isStarted = false;
        comp.toggleRunButton.setText("Start");
        return null;
    }

}

//Class to solve the TSP in the background.
class BackgroundSolver extends SwingWorker<Void, Void>{
    
    private GUI comp;
    private String fileName;

    public BackgroundSolver(GUI comp, String fileName){
        this.comp = comp;
        this.fileName = fileName;
    }

    @Override
    public Void doInBackground() {
        comp.importButton.setEnabled(false);
        if(comp.ts.calcTSP(comp.startingIndex.getText())){
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
        comp.clearPoints();
        comp.clearLines();
        String fp = comp.filePathInput.getText();
        String[] pathArr = fp.split("\\\\");
        int lastIdx = pathArr.length - 1;
        boolean isRead = comp.fr.readFile(fp);
        String fileName = pathArr[lastIdx];
        if(isRead){
            Collections.sort(comp.fr.cities, new CompareCities());
            comp.ts = new TSPSolver(comp);
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
    GUI comp;

    public runButtonAction(GUI comp){
        this.comp = comp;
    }

    public void actionPerformed(ActionEvent e){
        if(comp.isStarted){
            comp.toggleRunButton.setText("Start");
            comp.isStarted = false;
        }
        else{
            comp.toggleRunButton.setText("Stop");
            comp.isStarted = true;
            if(dec == null){
                dec = new BackgroundDecorator(comp);
                dec.execute();
            }
        }
        if(dec.finished){
            comp.clearLines();
            dec = new BackgroundDecorator(comp);
            dec.execute();
        }
    }
}

class CompareCities implements Comparator<City>{
    @Override
    public int compare(City c1, City c2){
        if(c1.y == c2.y){
            return((int)(c1.x - c2.x));
        }
        return((int)(c1.y - c2.y));
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

class Point{
    int x;
    int y;

    Point(int x, int y){
        this.x = x;
        this.y = y;
    }
}

class TSPSolver{
    public ArrayList<City> pathTaken = new ArrayList<City>();
    public ArrayList<City> cities;
    public double totalDistance = 0;
    public double maxX = 0;
    public double maxY = 0;
    public double minX = 999999999;
    public double minY = 999999999;
    private double scaleFactor = 1;
    private GUI comp;

    public TSPSolver(GUI comp){
        this.cities = comp.fr.cities;
        this.comp = comp;
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

    private void convertCity(City c){
        double xScale = ((maxX - minX)/800);
        double yScale = ((maxY - minY)/800);
        //Find which scale factor is larger so that all the cities fit within the screen
        scaleFactor = (xScale > yScale) ? xScale : yScale;
        int x1 = (int)Math.round((c.x - minX) / scaleFactor);
        int y1 = (int)Math.round((c.y - minY) / scaleFactor); 
        comp.points.add(new Point(x1, y1));
    }
    
    public double calcDistance(City c1, City c2){
        return Math.sqrt(Math.pow(c2.x - c1.x, 2) + Math.pow(c2.y - c1.y, 2));
    }

    public boolean calcTSP(String startingIdxString){
        int startingIdx;
        try{
            startingIdx = Integer.parseInt(startingIdxString);
            if(startingIdx > cities.size() - 1 || startingIdx < 0){
                startingIdx = 0;
            }
        }catch(NumberFormatException nfe){
            startingIdx = 0;
        }
        totalDistance = 0;
        City curCity = cities.get(startingIdx);
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
        for(City c : pathTaken){
            convertCity(c);
        }
        comp.repaint();
        totalDistance += calcDistance(pathTaken.get(0), pathTaken.get(pathTaken.size() - 1));
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

    private boolean isNumeric(String num){
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
