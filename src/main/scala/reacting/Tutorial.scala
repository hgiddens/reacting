package reacting

import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.TimerSupport
import japgolly.scalajs.react.vdom.prefix_<^._
import java.util.Date
import org.scalajs.dom.document
import scala.concurrent.duration._
import scala.scalajs.js
import scala.scalajs.js.JSApp
import scala.scalajs.js.annotation.JSName

@js.native
sealed trait MarkedOptions extends js.Object {
  val sanitize: Boolean = js.native
}
object MarkedOptions {
  def apply(sanitize: Boolean): MarkedOptions =
    js.Dynamic.literal(sanitize = sanitize).asInstanceOf[MarkedOptions]
}

@JSName("marked")
@js.native
object Marked extends js.Object {
  def apply(x: String): String = js.native
  def apply(x: String, options: MarkedOptions): String = js.native
}

// Using a var as the model for the data store as I'm too lazy to set up a server
object Model {
  final case class Comment(id: Int, author: String, text: String)
  private var ix = 1
  var comments = Seq.empty[Comment]
  def addComment(author: String, text: String): Seq[Comment] = {
    comments = comments :+ Model.Comment(ix, author, text)
    ix += 1
    comments
  }
  addComment("Pete Hunt", "This is one comment")
  addComment("Jordan Walke", "This is *another* comment")
}

object Tutorial extends JSApp {
  val Comment = ReactComponentB[Model.Comment]("Comment").
    stateless.
    noBackend.
    render_P {
      case Model.Comment(_, author, comment) =>
        val markdown = Marked(comment, MarkedOptions(sanitize = true))
        <.div(
          ^.className := "comment",
          <.h2(
            ^.className := "commentAuthor", author
          ),
          <.span(^.dangerouslySetInnerHtml(markdown))
        )
    }.build

  val CommentList = ReactComponentB[Seq[Model.Comment]]("CommentList").
    stateless.
    noBackend.
    render_P { data =>
      val commentNodes = data.map(Comment(_))
      <.div(^.className := "commentList", commentNodes)
    }.build

  // Hide more complicated component definitions inside an object
  object CommentForm {
    final case class Props(onCommentSubmit: (String, String) => Callback)
    final case class State(author: String, text: String)

    final class Backend($: BackendScope[Props, State]) {
      def handleAuthorChange(e: ReactEventI) = {
        val author = e.target.value
        $.modState(_.copy(author = author))
      }
      def handleTextChange(e: ReactEventI) = {
        val text = e.target.value
        $.modState(_.copy(text = text))
      }
      def handleSubmit(e: ReactEventI) =
        for {
          _ <- e.preventDefaultCB
          state <- $.state
          props <- $.props
          _ <- state match {
            case State(author, text) if author.nonEmpty && text.nonEmpty =>
              props.onCommentSubmit(author, text) >> $.setState(State("", ""))
            case _ =>
              Callback(())
          }
        } yield ()
      def render(s: State) =
        <.form(
          ^.className := "commentForm",
          ^.onSubmit ==> handleSubmit,
          <.input(
            ^.`type` := "text",
            ^.placeholder := "Your name",
            ^.value := s.author,
            ^.onChange ==> handleAuthorChange
          ),
          <.input(
            ^.`type` := "text",
            ^.placeholder := "Say something...",
            ^.value := s.text,
            ^.onChange ==> handleTextChange
          ),
          <.input(^.`type` := "submit", ^.value := "Post")
        )
    }

    val component = ReactComponentB[Props]("CommentForm").
      initialState(State("", "")).
      renderBackend[Backend].
      build

    def apply(onCommentSubmit: (String, String) => Callback) =
      component(Props(onCommentSubmit))
  }

  object CommentBox {
    type URI = String
    final case class Props(uri: URI, pollInterval: FiniteDuration)

    final class Backend($: BackendScope[Props, Seq[Model.Comment]]) extends TimerSupport {
      // Note that not wrapping Model.comments in CallbackTo results in this always fetching the same, initial seq
      def loadCommentsFromServer: Callback =
        for {
          current <- CallbackTo(Model.comments)
          _ <- $.setState(current)
        } yield ()

      def handleCommentSubmit(author: String, text: String): Callback =
        for {
          id <- CallbackTo(new Date().getTime.toInt)
          current <- $.state
          _ <- $.setState(current :+ Model.Comment(id, author, text))
          updated <- CallbackTo(Model.addComment(author, text))
          _ <- $.setState(updated)
        } yield ()

      def render(data: Seq[Model.Comment]) =
        <.div(
          ^.className := "commentBox",
          <.h1("Comments"),
          CommentList(data),
          CommentForm(onCommentSubmit = handleCommentSubmit)
        )
    }

    val component = ReactComponentB[Props]("CommentBox").
      initialState(Seq.empty[Model.Comment]).
      renderBackend[Backend].
      configure(TimerSupport.install).
      componentDidMount { $ =>
        $.backend.loadCommentsFromServer >> $.backend.setInterval($.backend.loadCommentsFromServer, $.props.pollInterval)
      }.build

    def apply(uri: URI, pollInterval: FiniteDuration) =
      component(Props(uri, pollInterval))
  }

  def main() =
    ReactDOM.render(CommentBox(uri = "/api/comments", pollInterval = 2.seconds), document.getElementById("content"))
}
