package todo.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import todo.connection.command.Commands;
import todo.connection.db.DbHandler;
import todo.connection.query.Query;
import todo.connection.response.OkResponse;
import todo.connection.response.Response;
import todo.data.user.User;
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

			Query query = new Gson().fromJson(sb.toString(), Query.class);
			if (query == null)
				throw new Exception("[ERROR] Query is null, data=" + sb.toString());
			if (query.getUser() == null)
				throw new Exception("[ERROR] User from query is null, data=" + sb.toString());

			PrintWriter out = response.getWriter();

			out.print(new Gson().toJson(createResponseForQuery(out, query)));
			out.flush();
		} catch (Exception e) {
			Trace.print("Servlet " + this.getServletName() + " >> @" + where + "= " + e.getMessage());
			e.printStackTrace();
		}
	}

	private Response createResponseForQuery(PrintWriter out, Query query) throws Exception {
		User user = query.getUser();
		checkUserInput(out, user);

		return Commands.getInstance().runCommand(query);
	}

	private void sendErrorResponse(PrintWriter out, String msg) throws Exception {
		checkPrintWriter(out);
		out.print(new Gson().toJson(new OkResponse(msg)));
	}

	private void checkPrintWriter(PrintWriter out) throws Exception {
		if (out == null)
			throw new Exception("[ERROR] Response can't be created because of a NullPointer");
	}

	private void checkUserInput(PrintWriter out, User user) throws Exception {
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
