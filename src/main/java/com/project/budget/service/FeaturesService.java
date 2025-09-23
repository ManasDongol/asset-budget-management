package com.project.budget.service;

import com.project.budget.entity.FeaturesEntity;
import com.project.budget.repository.FeaturesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FeaturesService {

    @Autowired
    private FeaturesRepository featuresRepository;

    public int getFeatureStatus(String featureName) {
        return featuresRepository.findByFeature(featureName)
                .map(FeaturesEntity::getStatus)
                .orElse(1); // default = Active
    }

    public void updateFeatureStatus(String featureName, boolean active) {
        FeaturesEntity feature = featuresRepository.findByFeature(featureName)
                .orElseGet(() -> new FeaturesEntity(featureName, 0));

        feature.setStatus(active ? 1 : 0);
        featuresRepository.save(feature);
    }
}
