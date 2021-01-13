/*package quickstart.gui

import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.{Button, CheckBox, ChoiceBox, Label, PasswordField, RadioButton, ScrollPane, TextField, ToggleGroup, Tooltip}
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.Scene
import scalafx.scene.input.{KeyCode, KeyEvent}
import scalafx.scene.layout.BorderPane

object ClientGui extends JFXApp {
  stage = new PrimaryStage {
    title = "Chat"
    scene = new Scene(800, 800) {
      root = new BorderPane {
        val chatArea: Label = new Label("Chat area.\n")
        val chatPane = new ScrollPane() {
          content = chatArea
        }
        val textBox: TextField = new TextField() {
          prefColumnCount = 50
          promptText = "Type message here"
          alignment = Pos.CenterLeft
          onKeyPressed = event => {
            event.consume()
            if (event.getCode.toString == KeyCode.Enter.toString()) {
              sendButton.fire()
            }
          }
        }
        val sendButton: Button = new Button("Send message") {
          minWidth = 120.0
          onMouseEntered = _ => {
            text = "Click me!"
          }
          onMouseExited = _ => {
            text = "Send message"
          }
          onAction = _ => {
            // here send the new message to the clienthandler
            if (chatArea.text.value.size > 0) {
              chatArea.setText(chatArea.text.value + textBox.text.value + "\n")
              textBox.text = ""
              textBox.requestFocus()
              chatPane.setVvalue(1.0) // todo: does not go all the way, meybe rework the borderpane to a hbox or add a hbox?
            }
          }
        }
        val messageWithButton: BorderPane = new BorderPane {
          left = textBox
          right = sendButton
        }
        center = chatPane
        bottom = messageWithButton
      }
    }
  }
}

 */