package fr.umontpellier.evo;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
         * Identify the modules in a dendrogram.
         * @param cp The minimal coupling of the modules.
         * @return A set of clusters, one for each module found.
         */
        public List<Cluster> identifyModules(int cp) {
            // Count the number of classes (cluster leaves)
            Set<Cluster> clusters = steps.stream()
                    .flatMap(s -> Stream.of(s.cluster1, s.cluster2))
                    .collect(Collectors.toSet());
            long totalNumberOfClasses = clusters.stream()
                    .filter(s -> s.classNames.size() == 1)
                    .count();

//            System.out.println("Steps:");
//            for (Step s : steps) {
//                System.out.println("|_ Step: weight=" + s.weight + ", cluster1=" + s.cluster1.classNames + ", cluster2=" + s.cluster2.classNames);
//            }

            // Initialization: one module containing every class (cluster)
            List<Cluster> modules = new ArrayList<>();
            int stepIndex = steps.size() - 1;
            Step step = steps.get(stepIndex);
            modules.add(step.mergedCluster);
            int numberOfModules = 1;

//            System.out.println("stepIndex=" + stepIndex + ", numberOfModules=" + numberOfModules + ", totalNumberOfClasses=" + totalNumberOfClasses + ", weight=" + step.weight + ", cp=" + cp);
            while (numberOfModules <= totalNumberOfClasses / 2) {
                if (stepIndex < 0) break;
                step = steps.get(stepIndex);
                if (step.weight > cp) break;
                modules.remove(step.mergedCluster);
                modules.add(step.cluster1);
                modules.add(step.cluster2);
                numberOfModules++;
                stepIndex--;
//                System.out.println("stepIndex=" + stepIndex + ", numberOfModules=" + numberOfModules + ", totalNumberOfClasses=" + totalNumberOfClasses + ", weight=" + step.weight + ", cp=" + cp);
            }

//            System.out.println("Total number of cluster leaves: " + totalNumberOfClasses);
//            System.out.println("Latest step: weight=" + steps.getLast().weight + ", cluster1=" + steps.getLast().cluster1.classNames + ", cluster2=" + steps.getLast().cluster2.classNames);
//            System.out.println("n-1 step: weight=" + steps.get(steps.size() - 2).weight + ", cluster1=" + steps.get(steps.size() - 2).cluster1.classNames + ", cluster2=" + steps.get(steps.size() - 2).cluster2.classNames);

//            System.out.println("Modules");
//            for (Cluster module : modules) {
//                System.out.println("|_ Module: " + module.classNames);
//            }

            return modules;
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
