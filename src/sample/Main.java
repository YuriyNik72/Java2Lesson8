package sample;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;



public class Main extends Application {




    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("sample.fxml"));
        Parent root = loader.load();
        primaryStage.setTitle("Chat 2021");
        primaryStage.setScene(new Scene(root, 600, 450));
        primaryStage.show();
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {

            public void handle(WindowEvent we) {
             Controller controller=loader.getController();
             controller.disconnect();
            }
        });

    }

    private final Object mon=new Object();
    private  char currentLetter='A';


    public static void main(String[] args) {
        launch(args);

        Main waitObj=new Main();
        Thread thread1=new Thread(()->{
            waitObj.printA();
        });
        Thread thread2=new Thread(()->{
            waitObj.printB();
        });
        Thread thread3=new Thread(()->{
            waitObj.printC();
        });
        thread1.start();
        thread2.start();
        thread3.start();

    }

    public void printA() {
        synchronized (mon){
            try{
                for (int i=0;i<5;i++){
                    while (currentLetter != 'A'){
                    mon.wait();
                     }
                    System.out.print("A");
                    currentLetter='B';
                    mon.notifyAll();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void printB() {
        synchronized (mon){
            try{
                for (int i=0;i<5;i++){
                    while (currentLetter != 'B'){
                        mon.wait();
                    }
                    System.out.print("B");
                    currentLetter='C';
                    mon.notifyAll();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void printC() {
        synchronized (mon){
            try{
                for (int i=0;i<5;i++){
                    while (currentLetter != 'C'){
                        mon.wait();
                    }
                    System.out.print("C ");
                    currentLetter='A';
                    mon.notifyAll();
                }
            } catch (InterruptedException e) {
                 e.printStackTrace();
            }
        }
    }

}