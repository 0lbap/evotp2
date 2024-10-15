package fr.umontpellier.evo;

import java.util.*;

public class ClassClusterizer {

    public static Dendrogram clusterize(Map<Set<String>, Integer> couplings, int total) {
        // Use a TreeMap to sort couplings by their weights in descending order
        TreeMap<Integer, List<Set<String>>> sortedCouplings = new TreeMap<>(Collections.reverseOrder());

        // Group the coupling sets by their weights
        for (Map.Entry<Set<String>, Integer> entry : couplings.entrySet()) {
            sortedCouplings.computeIfAbsent(entry.getValue(), k -> new ArrayList<>()).add(entry.getKey());
        }

        // Initialize clusters: every class starts as its own cluster
        Map<String, Cluster> clusters = new HashMap<>();
        for (Set<String> classPair : couplings.keySet()) {
            for (String className : classPair) {
                clusters.putIfAbsent(className, new Cluster(className));
            }
        }

        // Build the dendrogram by merging clusters based on coupling strength
        Dendrogram dendrogram = new Dendrogram();
        for (Map.Entry<Integer, List<Set<String>>> entry : sortedCouplings.entrySet()) {
            int weight = entry.getKey();
            List<Set<String>> pairs = entry.getValue();

            for (Set<String> pair : pairs) {
                Iterator<String> it = pair.iterator();
                String class1 = it.next();
                String class2 = it.next();

                Cluster cluster1 = clusters.get(class1);
                Cluster cluster2 = clusters.get(class2);

                if (cluster1 != cluster2) {
                    // Merge the two clusters
                    Cluster mergedCluster = cluster1.merge(cluster2, weight);
                    // Update the clusters map to point to the merged cluster
                    for (String className : mergedCluster.getClassNames()) {
                        clusters.put(className, mergedCluster);
                    }
                    dendrogram.addStep(cluster1, cluster2, mergedCluster, weight);
                }
            }
        }

        return dendrogram;
    }

    // Represents a cluster of classes
    static class Cluster {
        private final Set<String> classNames;
        private final List<Cluster> mergedClusters;

        public Cluster(String className) {
            this.classNames = new HashSet<>(Set.of(className));
            this.mergedClusters = new ArrayList<>();
        }

        public Cluster(Set<String> classNames, List<Cluster> mergedClusters) {
            this.classNames = classNames;
            this.mergedClusters = mergedClusters;
        }

        public Cluster merge(Cluster other, int weight) {
            Set<String> mergedClassNames = new HashSet<>(this.classNames);
            mergedClassNames.addAll(other.classNames);

            List<Cluster> newMergedClusters = new ArrayList<>(this.mergedClusters);
            newMergedClusters.addAll(other.mergedClusters);
            newMergedClusters.add(this);
            newMergedClusters.add(other);

            return new Cluster(mergedClassNames, newMergedClusters);
        }

        public Set<String> getClassNames() {
            return classNames;
        }
    }

    // Represents the dendrogram for hierarchical clustering
    static class Dendrogram {
        private final List<Step> steps;

        public Dendrogram() {
            this.steps = new ArrayList<>();
        }

        public void addStep(Cluster cluster1, Cluster cluster2, Cluster mergedCluster, int weight) {
            steps.add(new Step(cluster1, cluster2, mergedCluster, weight));
        }

        public List<Step> getSteps() {
            return this.steps;
        }

        // Represents a single step in the dendrogram
        static class Step {
            final Cluster cluster1;
            final Cluster cluster2;
            final Cluster mergedCluster;
            final int weight;

            public Step(Cluster cluster1, Cluster cluster2, Cluster mergedCluster, int weight) {
                this.cluster1 = cluster1;
                this.cluster2 = cluster2;
                this.mergedCluster = mergedCluster;
                this.weight = weight;
            }
        }
    }
}
