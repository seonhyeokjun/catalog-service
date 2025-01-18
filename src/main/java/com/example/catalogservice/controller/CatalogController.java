package com.example.catalogservice.controller;

import com.example.catalogservice.jpa.CatalogEntity;
import com.example.catalogservice.service.CatalogService;
import com.example.catalogservice.vo.ResponseCatalog;
import org.modelmapper.ModelMapper;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/catalog-service")
public class CatalogController {
    Environment env;
    CatalogService catalogService;
    private DiscoveryClient discoveryClient;

    public CatalogController(Environment env, CatalogService catalogService) {
        this.env = env;
        this.catalogService = catalogService;
    }

    @GetMapping("/health_check")
    public String status() {
        List<ServiceInstance> serviceList = getApplications();
        serviceList.forEach(serviceInstance -> {
            System.out.printf("instanceId: %s, serviceId: %s, host: %s, scheme: %s, uri:%s%n",
                    serviceInstance.getInstanceId(),
                    serviceInstance.getServiceId(),
                    serviceInstance.getHost(),
                    serviceInstance.getScheme(),
                    serviceInstance.getUri());
        });


        return String.format("It's Working in Catalog Service on PORT %s", env.getProperty("local.server.port"));
    }

    @GetMapping("/catalogs")
    public Object getCatalogs() {
        Iterable<CatalogEntity> catalogs = catalogService.getAllCatalogs();

        List<ResponseCatalog> result = new ArrayList<>();
        catalogs.forEach(v -> {
            result.add(new ModelMapper().map(v, ResponseCatalog.class));
        });

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    private List<ServiceInstance> getApplications() {
        List<String> services = discoveryClient.getServices();
        List<ServiceInstance> instances = new ArrayList<>();
        services.forEach(serviceName -> {
            List<ServiceInstance> instanceList = discoveryClient.getInstances(serviceName);
            instances.addAll(instanceList);
        });
        return instances;
    }
}
