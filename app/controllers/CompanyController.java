package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import models.Company;
import models.Computer;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Result;
import repository.CompanyRepository;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

public class CompanyController extends Controller {

    private final CompanyRepository companyRepository;
    private final FormFactory formFactory;

    @Inject
    public CompanyController(CompanyRepository companyRepository, FormFactory formFactory) {
        this.companyRepository = companyRepository;
        this.formFactory = formFactory;
    }

    public Result editCompany(Long id) throws ExecutionException, InterruptedException {
        Company byId = companyRepository.findById(id);
        Form<Company> fill = formFactory.form(Company.class).fill(byId);
        CompletionStage<Map<String, String>> options = companyRepository.options();
//        Map<String, String> stringStringMap = options.toCompletableFuture().get();
        return ok(views.html.editCompany.render(id,fill));
    }

    public Result processEdit() throws ExecutionException, InterruptedException {
        Form<Company> companyForm = formFactory.form(Company.class).bindFromRequest();
        Company company = companyForm.get();
        System.out.println(company);
        Company byName = companyRepository.findByName(company.name);
        HashMap<String, String> resp = new HashMap<>();
        if (byName!=null){
            resp.put("error", "such company already exists");
            resp.put("comp", byName.toString());
        }else{
            Company save = companyRepository.update(company);
            resp.put("success", save.toString());
        }
        JsonNode jsonNode = new ObjectMapper().valueToTree(resp);
        return ok(jsonNode);
    }

}
