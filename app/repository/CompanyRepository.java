package repository;

import io.ebean.Ebean;
import io.ebean.EbeanServer;
import io.ebean.Transaction;
import models.Company;
import models.Computer;
import play.db.ebean.EbeanConfig;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.supplyAsync;

/**
 *
 */
public class CompanyRepository {

    private final EbeanServer ebeanServer;
    private final DatabaseExecutionContext executionContext;

    @Inject
    public CompanyRepository(EbeanConfig ebeanConfig, DatabaseExecutionContext executionContext) {
        this.ebeanServer = Ebean.getServer(ebeanConfig.defaultServer());
        this.executionContext = executionContext;
    }

    public Company findById(Long id) {
        return ebeanServer.find(Company.class).setId(id).findOne();
    }

    public Company findByName(String name) {
        return ebeanServer.find(Company.class).where().eq("name", name).findOne();
    }

    public CompletionStage<Map<String, String>> options() {
        return supplyAsync(() -> ebeanServer.find(Company.class).orderBy("name").findList(), executionContext)
                .thenApply(list -> {
                    HashMap<String, String> options = new LinkedHashMap<String, String>();
                    for (Company c : list) {
                        options.put(c.id.toString(), c.name);
                    }
                    return options;
                });
    }

    public Company save(Company company) {
        company.id = System.currentTimeMillis(); // not ideal, but it works
        ebeanServer.insert(company);
        return company;
    }

    public Company update(Company company){
        Transaction txn = ebeanServer.beginTransaction();
        try {
            Company baseCompany = ebeanServer.find(Company.class).setId(company.id).findOne();
            Company byName = findByName(company.name);
            if (baseCompany != null && byName == null) {
                baseCompany.name = company.name;
                baseCompany.update();
                txn.commit();
                return baseCompany;
            }else {
                return null;
            }
        } finally {
            txn.end();
        }
    }

}
