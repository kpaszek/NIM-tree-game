package nimgame;
import java.util.*;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.scene.input.*;
import javafx.scene.Group;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.paint.Color;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.layout.VBox;
import javafx.scene.Parent;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.util.Duration;

class Global {
   public
           static boolean playerNow = true;
           static boolean animationFinished = true;
           static boolean isMouseOnNode = false;
}

abstract class Game
{
protected
        
    int game_size;
    boolean player_now;
    boolean misere;
    boolean isOver;
    
    abstract void Display();
    abstract void Poka();
    abstract void AI_Move(int i);
    abstract void Player_Move();  
    abstract boolean Is_Over();
            
    void setGameSize(int i) {
        game_size = i;
    }
            
    Game(int size){
        game_size = size;
        player_now = true;
        isOver = false;
    }
    
    
    void Play()
    {    
        while(!Is_Over())
        {
            //Poka();
            if(player_now)Player_Move();
            //else AI_Move();
            System.out.println("Nowy ruch!\n");
            player_now = !player_now;
        }
        if(player_now)System.out.println("Wygrana!\n");
        else System.out.println("Przegrana...\n");
    }
    
}

class Tree
{  
public 
    int[] nimber;
    Vector<Integer>[] tree;
    int[] parent;
    int[] distance;
    int tree_size;
    final Circle[] circ;
    final FadeTransition[] ft;
    final FadeTransition[] ftl;
    Line[] line;
    final Group group = new Group();
    boolean transitionPlaying = false;
    boolean klik = false;
    
    int Count_Nimber(Integer st)
    {
        nimber[st] = 0;
        for(int i = 0;i<tree[st].size();i++)
        {   
            int temp = Count_Nimber(tree[st].get(i));
            nimber[st] ^= (temp+1);
        }
        return nimber[st];
    }
    
    void Count_Move(Integer st,int rest)
    {
        int change = rest ^ nimber[st];
        for(int i = 0;i < tree[st].size();i++)
        {
            int temp = change ^ ( nimber[tree[st].get(i)] + 1); 
            if(temp == 0)
            {
                System.out.println( st + " " + (tree[st].get(i)));
                graphicalEraseNode(tree[st].get(i));
                return;
            }
            else if(temp < (nimber[tree[st].get(i)] + 1))
            {
                Count_Move(tree[st].get(i),temp-1);
                return;
            }
       }
    }
     
    void Erase(Integer Parent,Integer Child)
    {    
        int size = tree[Child].size();
        for(int i=0;i<size;i++) 
            Erase(Child,tree[Child].get(0));
        
        tree[Parent].removeElement(Child);
    }
    
    void graphicalEraseNode (final int i) {
        ParallelTransition pt = getTransitions(i);
        pt.onFinishedProperty().set(new EventHandler<ActionEvent>() {
        @Override 
            public void handle(ActionEvent actionEvent) {
                eraseNode(i);
                Global.animationFinished = true;
            }
        });
        pt.play();
        Display();
    }
    
    void eraseNode(int i) {
        int size = tree[i].size();
        tree[parent[i]].removeElement(i);
        for (int j=size-1; j>=0; j--) {
            eraseNode(tree[i].get(j));
        }
        
        group.getChildren().remove(circ[i]);
        if (i>0) group.getChildren().remove(line[i-1]);
    }
    
    void Display()
    {
       for(int i=0;i<tree_size;i++)
       {
           //if (tree[i].size()>0 || i==0) {
            System.out.printf("%d: ",i);
            for(int j=0;j<tree[i].size();j++)
              System.out.printf("%d ",tree[i].get(j));
            System.out.println("");
           //}
       }
    }
    
    ParallelTransition getTransitions(int i) {
        ParallelTransition pt = new ParallelTransition();
        for (int j=0; j<tree[i].size(); j++) {
            pt.getChildren().addAll(getTransitions(tree[i].get(j)));
        }
        pt.getChildren().add(ft[i]);
        pt.getChildren().add(ftl[i-1]);
        return pt;
    }
    
    void Connect() {
        int i = 0;
        int size = 0;
        while (i<tree_size) {
            for (int j=0; j<tree[i].size(); j++) {
                line[size]=new Line(circ[i].getCenterX(), circ[i].getCenterY(), circ[tree[i].get(j)].getCenterX(), circ[tree[i].get(j)].getCenterY());
                line[size].setStrokeWidth(6);
                line[size].setStroke(Color.BROWN);
                ftl[size] = new FadeTransition(Duration.millis(300), line[size]);
                ftl[size].setFromValue(1.0);
                ftl[size].setToValue(0.0);
                ftl[size].setCycleCount(1);
                size++;
            }
            i++;
        }
    }
    
    void Paint (int startX, int endX, int startY, int endY, int node) {
        int h = (endY-startY)/(distance[tree_size-1]+1);
        Paint_Wrapped(startX, endX, startY, h, node);
    }
    
    void Paint_Wrapped(Integer startX, Integer endX, Integer Y, Integer Height, final Integer node) {
       circ[node] = new Circle((startX+endX)/2, Y+(Height/2), 10);
       circ[node].setFill(Color.GREEN);
       circ[node].toFront();
       ft[node] = new FadeTransition(Duration.millis(300), circ[node]);
       ft[node].setFromValue(1.0);
       ft[node].setToValue(0.0);
       ft[node].setCycleCount(1);
       int numChildren = tree[node].size();
       
       for (int i = 0; i<numChildren; i++ ) {
           int newStart = startX+i*((endX-startX)/numChildren);
           int newEnd = startX+(i+1)*((endX-startX)/numChildren);
           int newY = Y+Height;
           
           Paint_Wrapped(newStart, newEnd, newY, Height, tree[node].get(i));
       }
    }
    
    Group getNodes () {
        for (int i = 0; i<tree_size; i++) {
            if (i!=tree_size-1) group.getChildren().add(line[i]);
        }
        for (int i = 0; i<tree_size; i++) {
            final int iFinal = i;
            final Circle c = circ[i];
            if (i!=0) c.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
                public void handle (MouseEvent event) {
                    if (event.isPrimaryButtonDown() && Global.animationFinished == true) {
                        Global.playerNow = !Global.playerNow;
                        Global.animationFinished = false;
                        graphicalEraseNode(iFinal);
                        Display();
                    }
                }
            });
            if (i!=0) c.setOnMouseEntered(new EventHandler<MouseEvent>() {
            @Override
                public void handle (MouseEvent event) {
                    c.setFill(Color.GREENYELLOW);
                    Global.isMouseOnNode = true;
                }
            });
            if (i!=0) c.setOnMouseExited(new EventHandler<MouseEvent>() {
            @Override
                public void handle (MouseEvent event) {
                    c.setFill(Color.GREEN);
                    Global.isMouseOnNode = false;
                }
            });
            group.getChildren().add(c);
        }
        return group;
    }
    
    int getLevel(int node) {
        int level = 0;
        for (int j=0; j<tree[node].size(); j++) {
            int test = getLevel(tree[node].get(j)) + 1;
            if (test>level) level = test;
        }
        
        return level;
    }
    
    void Generate()
    {   
        Random rand = new Random();
        int size = tree_size-1,k = 0;
        parent[0] = 0;
        for(int i = 0;i<tree_size-1;i++)
        {
            if(size != 0)
            {
               int mod = size < 3 ? size : 3;
               int v = rand.nextInt(mod)+1;
       
               for(int j=0;j<v;j++)
               {
                   tree[i].add(++k);
                   parent[k] = i;
                   distance[k] = distance[i] + 1;
               }
               size -= v;
            }
        }
    }
    
    Tree(int size)
    {   
        tree_size = size;
        nimber = new int[tree_size];
        tree = new Vector[tree_size];
        parent = new int[tree_size];
        distance = new int[tree_size];
        distance[0]=0;
        circ = new Circle[tree_size];
        ft = new FadeTransition[tree_size];
        ftl = new FadeTransition[tree_size];
        line = new Line[tree_size-1];
        for(int i =0;i<tree_size;i++)
            tree[i] = new Vector();
        
        Generate();
    }
    
    
}

class Nim extends Game
{
protected
    Tree[] forest;
    
    void AI_Move(int c)
    {   
       if (c == 0) {
           int res = 0;
       for(int i=0;i<game_size;i++)
           res ^= forest[i].Count_Nimber(0);
       
       if(res != 0)
       {
           for(int i=0;i<game_size;i++)
           {
                for(int j=0;j<forest[i].tree[0].size();j++)
                {  
                    int nimb = forest[i].nimber[forest[i].tree[0].get(j)]+1;
                    int change = res ^ nimb;
                    if( change == 0)
                    {
                        //forest[i].Erase(0,forest[i].tree[0].get(j));
                        forest[i].graphicalEraseNode(forest[i].tree[0].get(j));
                        return;
                    }
                    else if(change < nimb)
                    {
                        forest[i].Count_Move(forest[i].tree[0].get(j),change-1);
                        return;
                    }
                 }
           }
       }
       else
       {
          for(int i = 0;i<game_size;i++)
          {
            if(!forest[i].tree[0].isEmpty())
            {
                   //forest[i].Erase(0, forest[i].tree[0].get(0));
                   forest[i].graphicalEraseNode(forest[i].tree[0].get(0));
                   break;
            }
          }
       }
       } else if (c==1) {
           //AIBADURA
        int wynik=0,i,j,pom,magicznynimber,ruch,rozmiargry=game_size;
        //obliczenie stanu obecnego
        for(i=0;i<rozmiargry;i++)wynik^=forest[i].Count_Nimber(0);
 
        if(wynik!=0)
            //przejrzenie wszystkich drzew
            for(i=0;i<rozmiargry;i++)
                //i wszystkich galezi kazdego drzewa z pierwszego poziomu w poszukiwaniu ruchu
                for(j=0;j<forest[i].tree[0].size();j++){
                    pom=forest[i].tree[0].get(j);
                    magicznynimber=forest[i].nimber[pom]+1;
                    ruch=wynik^magicznynimber;
                   
                    if(ruch==0)
                        //znaleziono ruch
                        {forest[i].graphicalEraseNode(pom);return;}
                    else if(ruch<magicznynimber)
                        //szukaj w tym drzewie na wyzszym poziomie
                        {forest[i].Count_Move(pom,ruch-1);return;}}
         else
            //mozna usunac dowolne drzewo przy podstawie(jesli nie jest puste)
            for(i=0;i<rozmiargry;i++)
                if(!forest[i].tree[0].isEmpty())
                    {forest[i].graphicalEraseNode(forest[i].tree[0].get(0));break;}
       } else if (c==2) {
           Random a = new Random();
Random b = new Random();
int x = a.nextInt(game_size);
int y = b.nextInt(forest[x].tree_size);
while(true)
{
if(forest[x].tree[y].size()>0) {
forest[x].graphicalEraseNode(forest[x].tree[y].get(0));
break;
}
a = new Random();
x = a.nextInt(game_size);
b = new Random();
y = b.nextInt(forest[x].tree_size);
}
       } else if (c==3) {
           Random rand = new Random();
       int treeNumber = 0;
       int parentNumber = 0;
       int childNumber = 0;
       do treeNumber = rand.nextInt(game_size);
       while (forest[treeNumber].tree[0].size() <= 0);
       do parentNumber = rand.nextInt(forest[treeNumber].tree_size);
       while (forest[treeNumber].tree[parentNumber].size() <= 0);
       childNumber = rand.nextInt(forest[treeNumber].tree[parentNumber].size());
       int toRemove = forest[treeNumber].tree[parentNumber].get(childNumber);
       forest[treeNumber].graphicalEraseNode(toRemove);
       } else if (c==4) {
           int ile;
       int drzewo;
       int abcd2;
       ile=0;
       drzewo=(int) (Math.random()*game_size);
       
       abcd2=(int) (Math.random()*forest[drzewo].tree[0].size());
       for (int i=0;i<game_size;i++){
       if(forest[i].tree[0].size()!=0){    
       ile++;
       }
       }
       while(forest[drzewo].tree[0].size()==0&&ile>0){
            drzewo=(int) (Math.random()*game_size);
       }
               
       forest[drzewo].graphicalEraseNode(forest[drzewo].tree[0].get(abcd2));
       return;
       } else if (c==5) {
           Random rand = new Random();
       int treeNumber = 0;
       int parentNumber = 0;
       int childNumber = 0;
       do treeNumber = rand.nextInt(game_size);
       while (forest[treeNumber].tree[0].size() <= 0);
       do parentNumber = rand.nextInt(forest[treeNumber].tree_size);
       while (forest[treeNumber].tree[parentNumber].size() <= 0);
       childNumber = rand.nextInt(forest[treeNumber].tree[parentNumber].size());
       int toRemove = forest[treeNumber].tree[parentNumber].get(childNumber);
       forest[treeNumber].graphicalEraseNode(toRemove);
       }
    }
    
    void Player_Move()
    {   
       //AI_Move();
    }  
    
    boolean Is_Over()
    {
          for(int i=0;i<game_size;i++)
              if(!forest[i].tree[0].isEmpty()) {
                  isOver = false;
                  return false;
              }
          isOver = true;
          return true;
    }
    
    void Display()
    {    
       for(int i=0;i<game_size;i++)
       forest[i].Display();
    }
    
    void Poka() {
       Display();
       int width = 1200/game_size;
       for(int i=0;i<game_size;i++) {
        forest[i].Paint(i*width, (i+1)*width, 800, 0, 0);
        forest[i].Connect();
       }
    }
    
    Group getNodes() {
        Group nodes = new Group();
        for (int i = 0; i<game_size; i++) {
            nodes.getChildren().addAll(forest[i].getNodes());
        }
        return nodes;
    }

public
    Nim(boolean is_misere, int gameSize, int treeSize)
    {   
        super(gameSize);
        forest = new Tree[game_size];
        for(int i=0;i<game_size;i++)
        {
            forest[i] = new Tree(treeSize);
            
        }
        misere = is_misere;
        Poka();
    }
    
    void reconstruct(int gameSize, int treeSize) {
        isOver = false;
        game_size = gameSize;
        forest = new Tree[game_size];
        for(int i=0;i<game_size;i++)
        {
            forest[i] = new Tree(treeSize);
            
        }
        Poka();
    }
}

public class nimgame extends Application {
    int mode = -1;
    int player1AI = 1;
    int player2AI = 2;
    Nim p = new Nim(false, 3, 15);
    BooleanProperty booleanProperty = new SimpleBooleanProperty(false);
    final Label l = new Label();
    String playerOne = "Player 1";
    String playerTwo = "Player 2";
    Label winnerLabel = new Label("not_initialized");
    boolean test = false;
    
    public <P extends Parent> void fadeScreens(P box1, final P box2, final Scene scene, final Stage primaryStage)  {
            final DoubleProperty opacity = box1.opacityProperty();
        final DoubleProperty opacity2 = box2.opacityProperty();
        
        
         final Timeline fadeIn = new Timeline( 
                       new KeyFrame(Duration.ZERO, 
                              new KeyValue(opacity2, 0.0)),
                       new KeyFrame(new Duration(250), 
                              new KeyValue(opacity2, 1.0)));
         
         EventHandler onFinished = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                 box2.setOpacity(0.0);
                 primaryStage.setScene(scene);
                 fadeIn.play();
            }
        };
         final Timeline fade = new Timeline( 
           new KeyFrame(Duration.ZERO, new KeyValue(opacity,1.0)), 
           new KeyFrame(new Duration(200), onFinished, new KeyValue(opacity, 0.0)));
        fade.play();
    }
    
    final Timeline timer = new Timeline(new KeyFrame(Duration.seconds(1), new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (p.Is_Over() && Global.animationFinished == true) {
                    booleanProperty.set(true);
                    if (!Global.playerNow) {
                        winnerLabel.setText("Zwycięża " + playerOne);
                    }
                    else {
                        winnerLabel.setText("Zwycięża " + playerTwo);
                    }   
                }
                test = false;
                for (int i=0; i < p.game_size; i++) {
                    if (p.forest[i].tree[0].size() > 0) {
                        test = true;
                    }
                }
                if (test) {
                    if (Global.playerNow) p.AI_Move(player1AI);
                    else p.AI_Move(player2AI);
                }
                if (!p.Is_Over()) Global.playerNow = !Global.playerNow;
                booleanProperty.set(false);
                if (Global.playerNow) l.setText(playerOne);
                else l.setText(playerTwo);
            }
        }));
    
    public static void main(String[] args) {
       launch(args);
    }
    
    @Override
    public void start(final Stage primaryStage) {
        
        
        primaryStage.setTitle("Nim");
        
        final VBox box = new VBox(10);
        Label title = new Label("Nim Tree Game");
        Button pvp = new Button("Player vs Player");
        Button pvai = new Button("Player vs AI");
        Button aivai = new Button("AI vs AI");
        box.setAlignment(Pos.CENTER);
        box.getChildren().addAll(title, pvp, pvai, aivai);
        
        final VBox box2 = new VBox(10);
        Label tfLabel1 = new Label("Liczba drzew");
        tfLabel1.setStyle("-fx-font-size: 20");
        Label tfLabel2 = new Label("Rozmiar drzewa");
        tfLabel2.setStyle("-fx-font-size: 20");
        final TextField gameSizeTF = new TextField();
        gameSizeTF.setMaxWidth(170);
        gameSizeTF.setAlignment(Pos.CENTER);
        final TextField treeSizeTF = new TextField();
        treeSizeTF.setMaxWidth(170);
        treeSizeTF.setAlignment(Pos.CENTER);
        final Button b3 = new Button("Wpisz imiona");
        Button b4 = new Button("Powrót");
        box2.setAlignment(Pos.CENTER);
        box2.getChildren().addAll(tfLabel1, gameSizeTF, tfLabel2, treeSizeTF, b3, b4);
        
        final VBox box3 = new VBox(10);
        Label tfLabel3 = new Label("Imię gracza 1");
        tfLabel1.setStyle("-fx-font-size: 20");
        Label tfLabel4 = new Label("Imię gracza 2");
        tfLabel2.setStyle("-fx-font-size: 20");
        final TextField playerOneTF = new TextField();
        playerOneTF.setMaxWidth(170);
        playerOneTF.setAlignment(Pos.CENTER);
        final TextField playerTwoTF = new TextField();
        playerTwoTF.setMaxWidth(170);
        playerTwoTF.setAlignment(Pos.CENTER);
        Button b5 = new Button("Rozpocznij grę");
        Button b6 = new Button("Powrót");
        box3.setAlignment(Pos.CENTER);
        box3.getChildren().addAll(tfLabel3, playerOneTF, tfLabel4, playerTwoTF, b5, b6);
        
        final VBox box4 = new VBox(10);
        Label koniecLabel = new Label("KONIEC GRY");
        koniecLabel.setTextFill(Color.RED);
        Button newGameButton = new Button("Nowa gra");
        Button exitButton = new Button("Wyjdź");
        box4.setAlignment(Pos.CENTER);
        box4.getChildren().addAll(koniecLabel, winnerLabel, newGameButton, exitButton);
        
        final GridPane grid = new GridPane();
        final VBox rbBox1 = new VBox(10);
        final VBox rbBox2 = new VBox(10);
        final VBox buttonBoxL = new VBox(10);
        final VBox buttonBoxR = new VBox(10);
        final ToggleGroup tg1 = new ToggleGroup();
        final ToggleGroup tg2 = new ToggleGroup();
        RadioButton rb0 = new RadioButton("Krzysztof");
        rb0.setToggleGroup(tg1);
        rb0.setUserData(0);
        RadioButton rb1 = new RadioButton("Jan");
        rb1.setToggleGroup(tg1);
        rb1.setUserData(1);
        RadioButton rb2 = new RadioButton("Łukasz");
        rb2.setToggleGroup(tg1);
        rb2.setUserData(2);
        RadioButton rb3 = new RadioButton("Roman");
        rb3.setToggleGroup(tg1);
        rb3.setUserData(3);
        RadioButton rb4 = new RadioButton("Jakub");
        rb4.setToggleGroup(tg1);
        rb4.setUserData(4);
        RadioButton rb5 = new RadioButton("Krystian");
        rb5.setToggleGroup(tg1);
        rb5.setUserData(5);
        final String[] names = {"Krzysztof", "Jan", "Łukasz", "Roman", "Jakub", "Krystian"};
        tg1.selectedToggleProperty().addListener(new ChangeListener<Toggle>(){
            public void changed(ObservableValue<? extends Toggle> ov,
            Toggle old_toggle, Toggle new_toggle) {
            if (tg1.getSelectedToggle() != null) {
                int selected = Integer.parseInt(tg1.getSelectedToggle().getUserData().toString());
                player1AI = selected;
                playerOne = names[selected];
            }                
            }
        });
        rbBox1.getChildren().addAll(rb0, rb1, rb2, rb3, rb4, rb5);
        rbBox1.setAlignment(Pos.CENTER);
        RadioButton rb6 = new RadioButton("Krzysztof");
        rb6.setToggleGroup(tg2);
        rb6.setUserData(0);
        RadioButton rb7 = new RadioButton("Jan");
        rb7.setToggleGroup(tg2);
        rb7.setUserData(1);
        RadioButton rb8 = new RadioButton("Łukasz");
        rb8.setToggleGroup(tg2);
        rb8.setUserData(2);
        RadioButton rb9 = new RadioButton("Roman");
        rb9.setToggleGroup(tg2);
        rb9.setUserData(3);
        RadioButton rb10 = new RadioButton("Jakub");
        rb10.setToggleGroup(tg2);
        rb10.setUserData(4);
        RadioButton rb11 = new RadioButton("Krystian");
        rb11.setToggleGroup(tg2);
        rb11.setUserData(5);
        tg2.selectedToggleProperty().addListener(new ChangeListener<Toggle>(){
            public void changed(ObservableValue<? extends Toggle> ov,
            Toggle old_toggle, Toggle new_toggle) {
            if (tg1.getSelectedToggle() != null) {
                int selected = Integer.parseInt(tg2.getSelectedToggle().getUserData().toString());
                player2AI = selected;
                playerTwo = names[selected];
            }                
            }
        });
        rbBox2.getChildren().addAll(rb6, rb7, rb8, rb9, rb10, rb11);
        rbBox2.setAlignment(Pos.CENTER);
        
        Label player1Label = new Label("Gracz 1");
        Label player2Label = new Label("Gracz 2");
        Button b7 = new Button("Dalej");
        Button b8 = new Button("Powrót");
        buttonBoxL.getChildren().addAll(b8);
        buttonBoxL.setAlignment(Pos.CENTER);
        buttonBoxR.getChildren().addAll(b7);
        buttonBoxR.setAlignment(Pos.CENTER);
        grid.setHgap(50);
        grid.setVgap(20);
        grid.add(player1Label, 0, 0);
        grid.add(player2Label, 1, 0);
        grid.add(rbBox1, 0, 1);
        grid.add(rbBox2, 1, 1);
        grid.add(buttonBoxL, 0, 2);
        grid.add(buttonBoxR, 1, 2);
        grid.setAlignment(Pos.CENTER);
        
        
        final Scene oponentSelection = new Scene(box, 1200, 800);
        final Scene gameSizeSelection = new Scene(box2, 1200, 800);
        final Scene nameInput = new Scene(box3, 1200, 800);
        final Scene endGame = new Scene(box4, 1200, 800);
        final Scene AISelection = new Scene(grid, 1200, 800);
        oponentSelection.getStylesheets().add(nimgame.class.getResource("splash.css").toExternalForm());
        gameSizeSelection.getStylesheets().add(nimgame.class.getResource("splash.css").toExternalForm());
        nameInput.getStylesheets().add(nimgame.class.getResource("splash.css").toExternalForm());
        endGame.getStylesheets().add(nimgame.class.getResource("splash.css").toExternalForm());
        AISelection.getStylesheets().add(nimgame.class.getResource("splash.css").toExternalForm());
        
        final Group root = p.getNodes();
        final HBox stack = new HBox();
        
        final Scene gameScene = new Scene(root, 1200, 800);
        gameScene.setOnMousePressed(mouseHandler);
        
        
        
        l.setFont(new Font("Arial", 30));
        stack.getChildren().add(l);
        root.getChildren().add(stack);
        primaryStage.setScene(oponentSelection);
        primaryStage.show();
        
        pvp.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent event) {
                mode = 1;
                fadeScreens(box, box2, gameSizeSelection, primaryStage);
            }
        });
        
        pvai.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent event) {
                mode = 2;
                timer.setCycleCount(1);
                fadeScreens(box, box2, gameSizeSelection, primaryStage);
            }
        });
        
        aivai.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent event) {
                mode = 3;
                timer.setCycleCount(Timeline.INDEFINITE);
                fadeScreens(box, grid, AISelection, primaryStage);
            }
        });
        b3.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent event) {
                int a = 2;
                int b = 10;
                if (!gameSizeTF.getText().isEmpty()) a = Integer.parseInt(gameSizeTF.getText());
                if (!treeSizeTF.getText().isEmpty()) b = Integer.parseInt(treeSizeTF.getText());
                //p.Is_Over();
                Global.playerNow = true;
                p.reconstruct(a, b);
                root.getChildren().clear();
                root.getChildren().addAll(p.getNodes());
                root.getChildren().add(stack);
                if (mode!=3) fadeScreens(box2, box3, nameInput, primaryStage);
                else {
                    l.setText(playerOne);
                    fadeScreens(box2, root, gameScene, primaryStage);
                    timer.play();
                }
            }
        });
        b4.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent event) {
                if (mode == 3) fadeScreens(box2, grid, AISelection, primaryStage);
                else fadeScreens(box2, box, oponentSelection, primaryStage);
            }
        });
        b5.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent event) {
                if (!playerOneTF.getText().isEmpty()) playerOne = playerOneTF.getText();
                if (!playerTwoTF.getText().isEmpty()) playerTwo = playerTwoTF.getText();
                l.setText(playerOne);
                fadeScreens(box3, root, gameScene, primaryStage);
                if (mode == 3) timer.play();
            }
        });
        b6.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent event) {
                fadeScreens(box3, box2, gameSizeSelection, primaryStage);
            }
        });
        b7.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent event) {
                b3.setText("Rozpocznij grę");
                fadeScreens(grid, box2, gameSizeSelection, primaryStage);
            }
        });
        b8.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent event) {
                b3.setText("Wpisz imiona");
                fadeScreens(grid, box, oponentSelection, primaryStage);
            }
        });
        newGameButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent event) {
                fadeScreens(box4, box, oponentSelection, primaryStage);
            }
        });
        exitButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent event) {
                Platform.exit();
            }
        });
        
        booleanProperty.addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                booleanProperty.set(false);
                timer.stop();
                fadeScreens(root, box4, endGame, primaryStage);
            }
        });
    }
    
    EventHandler<MouseEvent> mouseHandler = new EventHandler<MouseEvent>() {
        @Override
        public void handle (MouseEvent mouseEvent) {
            if (p.Is_Over() && Global.animationFinished == true) {
                booleanProperty.set(true);
                if (!Global.playerNow) {
                    winnerLabel.setText("Zwycięża " + playerOne);
                }
                else {
                    winnerLabel.setText("Zwycięża " + playerTwo);
                }
                return;
            }
            if (Global.playerNow) l.setText(playerOne);
            else l.setText(playerTwo);
            if (mode == 2 && Global.isMouseOnNode == true) timer.play();
        }
    };
}

