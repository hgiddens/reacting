package reacting

import japgolly.scalajs.react.{PropsChildren, ReactNode, ReactComponentB, ReactDOM}
import japgolly.scalajs.react.vdom.prefix_<^._
import org.scalajs.dom.document

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

object Model {
  final case class Comment(id: Int, author: String, text: String)
}

object Tutorial extends JSApp {
  val data = Seq(
    Model.Comment(1, "Pete Hunt", "This is one comment"),
    Model.Comment(2, "Jordan Walke", "This is *another* comment")
  )

  object Comment {
    val component = ReactComponentB[Model.Comment]("Comment").
      stateless.
      noBackend.
      render_P {
        case Model.Comment(_, author, comment) =>
          <.div(
            ^.className := "comment",
            <.h2(
              ^.className := "commentAuthor", author
            ),
            <.span(^.dangerouslySetInnerHtml(Marked(comment, MarkedOptions(sanitize = true))))
          )
      }.build
    def apply(comment: Model.Comment) =
      component(comment)
  }

  val CommentList = ReactComponentB[Seq[Model.Comment]]("CommentList").
    stateless.
    noBackend.
    render_P { data =>
      val commentNodes = data.map(Comment(_))
      <.div(^.className := "commentList", commentNodes)
    }.build

  val CommentForm = ReactComponentB[Unit]("CommentForm").
    stateless.
    noBackend.
    render { $ =>
      <.div(^.className := "commentForm", "Hello world! I am a CommentForm.")
    }.buildU

  val CommentBox = ReactComponentB[Seq[Model.Comment]]("CommentBox").
    stateless.
    noBackend.
    render_P { data =>
      <.div(
        ^.className := "commentBox",
        <.h1("Comments"),
        CommentList(data),
        CommentForm()
      )
    }.build

  def main() =
    ReactDOM.render(CommentBox(data), document.getElementById("content"))
}
