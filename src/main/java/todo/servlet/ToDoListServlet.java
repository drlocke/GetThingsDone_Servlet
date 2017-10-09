package todo.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import todo.connection.command.Commands;
import todo.connection.db.DbHandler;
import todo.connection.gson.GsonUtils;
import todo.connection.query.Query;
import todo.connection.response.OkResponse;
import todo.connection.response.Response;
import todo.data.user.User;
import todo.errors.MessageLogException;
import todo.utils.Trace;

@WebServlet(name = "TodoListServlet", urlPatterns = { "/todolist" })
public class ToDoListServlet extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// processInput(request, response, "doGet");
	}

	@Override
	protected void doPut(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		processInput(request, response, "doPut");
	}

	private void processInput(HttpServletRequest request, HttpServletResponse response, String where)
			throws IOException {
		response.setContentType("application/json;charset=UTF-8");

		StringBuffer sb = new StringBuffer();
		String line = null;
		try {
			BufferedReader reader = request.getReader();
			while ((line = reader.readLine()) != null)
				sb.append(line);

			Query query = GsonUtils.getInstance().fromJsonToQuery(sb.toString());
			if (query == null)
				throw new MessageLogException("Query is null, data=" + sb.toString());
			if (query.getUser() == null)
				throw new MessageLogException("User from query is null, data=" + sb.toString());

			PrintWriter out = response.getWriter();

			out.print(GsonUtils.getInstance().toJson(createResponseForQuery(out, query)));
			out.flush();
		} catch (MessageLogException e) {
			Trace.print("Servlet " + this.getServletName() + " >> @" + where + "= " + e.getMessage());
			e.printStackTrace();
		}
	}

	private Response createResponseForQuery(PrintWriter out, Query query) throws MessageLogException {
		User user = query.getUser();
		checkUserInput(out, user);

		return Commands.getInstance().runCommand(query);
	}

	private void sendErrorResponse(PrintWriter out, String msg) throws MessageLogException {
		checkPrintWriter(out);
		out.print(GsonUtils.getInstance().toJson(new OkResponse(msg)));
	}

	private void checkPrintWriter(PrintWriter out) throws MessageLogException {
		if (out == null)
			throw new MessageLogException("Response can't be created because of a NullPointer");
	}

	private void checkUserInput(PrintWriter out, User user) throws MessageLogException {
		if (user == null)
			sendErrorResponse(out, "User data is null!");
		if (user.getName() == null)
			sendErrorResponse(out, "Username is null!");
		if (user.getName() == null)
			sendErrorResponse(out, "Password is null!");
	}

	@Override
	public void init() throws ServletException {
		Trace.init();
		Trace.print("Servlet " + this.getServletName() + " has started");
	}

	@Override
	public void destroy() {
		Trace.print("Servlet " + this.getServletName() + " has stopped");
		DbHandler.getSessionFactory().close();
	}

}
