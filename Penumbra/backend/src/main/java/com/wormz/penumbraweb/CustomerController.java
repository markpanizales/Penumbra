package com.wormz.penumbraweb;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Transaction;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by markanthonypanizales on 4/11/15.
 */
@Controller
@RequestMapping("/customer")
public class CustomerController {
    private final static String USER = "user";
    private static final String EMAIL = "email";
    private static final String PASSWORD = "password";
    private static final String ERROR = "Error: ";
    private static final String FAIL = "fail";


    //DI via Spring
    String message;

    private String mEmail;

    @RequestMapping(value="/{name}", method = RequestMethod.GET)
    public String getMovie(@PathVariable String name, ModelMap model) {

        model.addAttribute("movie", name);
        model.addAttribute("message", this.message);

        //retrun to jsp page, configurated in mvc-dispatcher-servlet.xml, view resolver
        return "list";

    }

    public void setMessage(String message) {
        this.message = message;
    }

    @RequestMapping(value="/addCustomerPage", method = RequestMethod.GET)
    public String getAddCustomerPage(ModelMap model) {

        return "add";

    }

    @RequestMapping(value="/signin", method = RequestMethod.POST)
    public ModelAndView signin(HttpServletRequest request, ModelMap model) {

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        String email = request.getParameter(EMAIL);
        String password = request.getParameter(PASSWORD);

        Query.Filter emailFilter = new Query.FilterPredicate(EMAIL, Query.FilterOperator.EQUAL,
                email);

        Query q = new Query(USER).setFilter(emailFilter);

        // Use prepared query
        PreparedQuery pq = datastore.prepare(q);

        Entity result = pq.asSingleEntity();

        if (result == null) {
            return null;
        }

        mEmail = email;

        return new ModelAndView("redirect:main");

    }

    //get all customers
    @RequestMapping(value="/main", method = RequestMethod.GET)
    public String mainMenu(HttpServletRequest request, ModelMap model) {

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        String email = mEmail;
        Query.Filter emailFilter = new Query.FilterPredicate(EMAIL, Query.FilterOperator.EQUAL,
                email);

        Query q = new Query(USER).setFilter(emailFilter);

        // Use prepared query
        PreparedQuery pq = datastore.prepare(q);

        Entity result = pq.asSingleEntity();

        System.out.print("Result: " + result);

        if (result == null) {
            return null;
        }



        model.addAttribute("user", result);

        return "main";

    }

    @RequestMapping(value="/add", method = RequestMethod.POST)
    public ModelAndView add(HttpServletRequest request, ModelMap model) {

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Transaction txn = datastore.beginTransaction();

        String email = request.getParameter("email");
        String password = request.getParameter("password");

        // encrypt password using bcrypt with salt
        String hash = BCrypt.hashpw(password, BCrypt.gensalt());


        //Key userKey = KeyFactory.createKey(USER, email);

        try {
            Date date = new Date();
            //Entity customer = new Entity(USER, userKey);
            Entity userEntity = new Entity(USER);
            userEntity.setProperty("email", email);
            userEntity.setProperty("password", hash);
            userEntity.setProperty("date", date);

            datastore.put(userEntity);

            txn.commit();
        }
        finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }

        return new ModelAndView("redirect:list");

    }

    //get all customers
    @RequestMapping(value="/list", method = RequestMethod.GET)
    public String listCustomer(ModelMap model) {

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Query query =
                new Query(USER).addSort("date", Query.SortDirection.DESCENDING);
        List<Entity> customers =
                datastore.prepare(query).asList(FetchOptions.Builder.withLimit(10));

        model.addAttribute("customerList", customers);

        return "list";

    }

}
