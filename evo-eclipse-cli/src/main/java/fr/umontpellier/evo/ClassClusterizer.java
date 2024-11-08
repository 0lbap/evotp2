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

        // Calculate the average coupling of this cluster
        public double getAverageCoupling(Map<Set<String>, Integer> couplings) {
            int sumCoupling = 0;
            int count = 0;
            for (String class1 : classNames) {
                for (String class2 : classNames) {
                    if (!class1.equals(class2)) {
                        Set<String> pair = Set.of(class1, class2);
                        if (couplings.containsKey(pair)) {
                            sumCoupling += couplings.get(pair);
                            count++;
                        }
                    }
                }
            }
            return count > 0 ? (double) sumCoupling / count : 0;
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

        /**
         * Get the top clusters that represent the modules of the application.
         * Selects the top maxModules number of clusters based on the dendrogram hierarchy.
         * Ensures that the clusters are non-overlapping and belong to different branches.
         */
        public Set<Cluster> getTopModules(int maxModules) {
            // Start with all clusters as separate modules
            List<Cluster> finalModules = new ArrayList<>();
            Set<Cluster> currentClusters = new HashSet<>();
            for (Step step : steps) {
                currentClusters.add(step.mergedCluster);
            }

            // Ensure we do not exceed the number of modules
            while (finalModules.size() < maxModules && !currentClusters.isEmpty()) {
                Cluster largestCluster = selectLargestCluster(currentClusters);
                finalModules.add(largestCluster);
                // Remove all subclusters from consideration
                currentClusters.removeAll(largestCluster.mergedClusters);
                currentClusters.remove(largestCluster);
            }

            return new HashSet<>(finalModules);
        }

        /**
         * Selects the largest cluster from the set of current clusters.
         * @param clusters The set of clusters to choose from.
         * @return The largest cluster.
         */
        private Cluster selectLargestCluster(Set<Cluster> clusters) {
            return clusters.stream().max(Comparator.comparingInt(c -> c.getClassNames().size())).orElseThrow();
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
