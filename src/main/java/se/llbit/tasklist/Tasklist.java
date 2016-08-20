/* Copyright (c) 2016 Jesper Öqvist <jesper@llbit.se>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *   1. Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *
 *   2. Redistributions in binary form must reproduce the above
 *      copyright notice, this list of conditions and the following
 *      disclaimer in the documentation and/or other materials provided
 *      with the distribution.
 *
 *   3. The name of the author may not be used to endorse or promote
 *      products derived from this software without specific prior
 *      written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN
 * IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package se.llbit.tasklist;

import javafx.application.Application;
import javafx.beans.InvalidationListener;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * This is a very simple tasklist application. It displays a list of tasks
 * in an always-on-top window. Tasks can be added, marked done, and deleted.
 */
public class Tasklist extends Application {
  private static final int PAD = 10;
  protected double dragOffsetX = 0;
  protected double dragOffsetY = 0;

  public static void main(String[] args) {
    launch();
  }

  @Override public void start(Stage stage) throws Exception {
    stage.setTitle("Tasklist");
    stage.initStyle(StageStyle.TRANSPARENT);
    stage.setAlwaysOnTop(true);

    VBox taskList = new VBox();
    taskList.setPadding(new Insets(0, 0, 0, 10));

    Font taskFont = new Font(18);

    TextField newEntry = new TextField();
    newEntry.setOnAction(event -> {
      String text = newEntry.getText();
      if (!text.isEmpty()) {
        newEntry.setText("");
        HBox task = new HBox();
        StackPane stack = new StackPane();
        stack.setAlignment(Pos.CENTER_LEFT);
        TextField editor = new TextField(text);
        editor.setVisible(false);
        editor.setOnAction(e -> editor.setVisible(false));
        editor.focusedProperty().addListener((observable, oldValue, newValue) -> {
          if (!newValue) {
            editor.setVisible(false);
          }
        });
        Label label = new Label(text);
        editor.textProperty().addListener(
            (observable, oldValue, newValue) -> label.setText(newValue));
        stack.getChildren().addAll(label, editor);
        ContextMenu menu = new ContextMenu();
        MenuItem future = new MenuItem("future");
        MenuItem current = new MenuItem("current");
        MenuItem done = new MenuItem("done");
        MenuItem delete = new MenuItem("delete");
        MenuItem up = new MenuItem("up");
        MenuItem down = new MenuItem("down");
        menu.getItems().addAll(future, current, done, delete, up, down);
        stack.setOnMouseClicked(e -> {
          if (e.getButton() == MouseButton.SECONDARY) {
            menu.show(stack, e.getScreenX(), e.getScreenY());
          }
        });
        future.setOnAction(e -> label.setTextFill(Color.WHITE));
        current.setOnAction(e -> label.setTextFill(Color.YELLOW));
        done.setOnAction(e -> label.setTextFill(Color.LIGHTGREEN));
        up.setOnAction(e -> {
          int index = taskList.getChildren().indexOf(task);
          if (index > 0) {
            taskList.getChildren().remove(index);
            taskList.getChildren().add(index - 1, task);
          }
        });
        down.setOnAction(e -> {
          int index = taskList.getChildren().indexOf(task);
          if (index < taskList.getChildren().size() - 1) {
            taskList.getChildren().remove(index);
            taskList.getChildren().add(index + 1, task);
          }
        });
        delete.setOnAction(e -> taskList.getChildren().remove(task));

        Button taskOptions = new Button("...");
        taskOptions.setVisible(false);
        taskOptions.setOnAction(e -> {
          Point2D point = taskOptions.localToScreen(0, 0);
          menu.show(taskList, point.getX(), point.getY() + taskOptions.getHeight());
        });
        label.setMinWidth(150);
        label.setFont(taskFont);
        label.setTextFill(Color.WHITE);
        task.setOnMouseEntered(e -> taskOptions.setVisible(true));
        task.setOnMouseExited(e -> taskOptions.setVisible(false));
        label.setOnMouseClicked(e -> {
          if (e.isStillSincePress() && e.getButton() == MouseButton.PRIMARY) {
            editor.setVisible(true);
            editor.requestFocus();
          }
        });
        task.getChildren().addAll(stack, taskOptions);
        taskList.getChildren().add(task);
      }
    });

    VBox vBox = new VBox();
    vBox.setAlignment(Pos.TOP_LEFT);
    vBox.setPadding(new Insets(15));
    vBox.getChildren().addAll(titleItem(stage, "TODO"), taskList, newEntry);

    InvalidationListener resizeListener = observable -> {
      Bounds bounds = vBox.getLayoutBounds();
      if (bounds.getWidth() > stage.getWidth()) {
        stage.setWidth(bounds.getWidth() + PAD * 2);
      }
      if (bounds.getHeight() > stage.getHeight()) {
        stage.setHeight(bounds.getHeight() + PAD * 2);
      }
    };
    vBox.widthProperty().addListener(resizeListener);
    vBox.heightProperty().addListener(resizeListener);

    Pane root = new Pane();
    root.setPadding(new Insets(PAD));
    root.setMinWidth(200);
    root.setMinHeight(200);
    root.getChildren().add(vBox);
    root.setBackground(Background.EMPTY);

    vBox.setBackground(new Background(new BackgroundFill(Color.rgb(140, 140, 140, 0.6),
        new CornerRadii(4), Insets.EMPTY)));
    vBox.setLayoutX(PAD);
    vBox.setLayoutY(PAD);
    root.setEffect(new DropShadow());

    stage.focusedProperty().addListener((observable, oldValue, newValue) -> {
      newEntry.setVisible(newValue);
    });

    Scene scene = new Scene(root);
    scene.setFill(null);
    vBox.setOnMousePressed(event -> {
      dragOffsetX = event.getScreenX() - stage.getX();
      dragOffsetY = event.getScreenY() - stage.getY();
    });
    vBox.setOnMouseDragged(event -> {
      stage.setX(event.getScreenX() - dragOffsetX);
      stage.setY(event.getScreenY() - dragOffsetY);
    });
    stage.setScene(scene);
    stage.show();
  }

  private Node titleItem(Stage stage, String titleText) {
    Button minimize = new Button("min");
    minimize.setVisible(false);
    minimize.setOnAction(e -> stage.setIconified(true));
    TextField editor = new TextField(titleText);
    editor.setVisible(false);
    Label title = new Label(titleText);
    title.setMinWidth(150);
    title.setTextFill(Color.WHITE);
    title.setFont(Font.font(null, FontWeight.BOLD, 18));
    title.setOnMouseClicked(event -> {
      if (event.isStillSincePress() && event.getButton() == MouseButton.PRIMARY) {
        editor.setVisible(true);
        editor.requestFocus();
      }
    });
    editor.setOnAction(e -> editor.setVisible(false));
    editor.focusedProperty().addListener((observable, oldValue, newValue) -> {
      if (!newValue) {
        editor.setVisible(false);
      }
    });
    editor.textProperty().addListener(
        (observable, oldValue, newValue) -> title.setText(newValue));
    editor.focusedProperty().addListener((observable, oldValue, newValue) -> {
      if (!newValue) {
        editor.setVisible(false);
      }
    });
    StackPane stack = new StackPane();
    stack.setAlignment(Pos.CENTER_LEFT);
    stack.getChildren().addAll(title, editor);
    HBox hBox = new HBox();
    hBox.getChildren().addAll(stack, minimize);
    hBox.setOnMouseEntered(e -> minimize.setVisible(true));
    hBox.setOnMouseExited(e -> minimize.setVisible(false));
    MenuItem minimizeItem = new MenuItem("minimize");
    minimizeItem.setOnAction(e -> stage.setIconified(true));
    MenuItem quit = new MenuItem("quit");
    quit.setOnAction(e -> stage.close());
    ContextMenu menu = new ContextMenu();
    menu.getItems().setAll(minimizeItem, quit);
    hBox.setOnMouseClicked(event -> {
      if (event.getButton() == MouseButton.SECONDARY) {
        menu.show(hBox, event.getScreenX(), event.getScreenY());
      }
    });
    return hBox;
  }
}
