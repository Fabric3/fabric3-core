/*
 * Fabric3
 * Copyright (c) 2009-2015 Metaform Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.api.host.failure;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import org.fabric3.api.host.contribution.ArtifactValidationFailure;

/**
 * Utility methods for outputting validation errors.
 */
public final class ValidationUtils {
    private static Comparator<Failure> COMPARATOR = (first, second) -> {
        if (first instanceof Failure && !(second instanceof Failure)) {
            return -1;
        } else if (!(first instanceof Failure) && second instanceof Failure) {
            return 1;
        } else {
            return 0;
        }
    };

    private static enum TYPE {
        WARNING,
        ERROR
    }

    private ValidationUtils() {
    }

    /**
     * Sorts and writes the list of error messages to a string.
     *
     * @param failures the collection of failures to write
     * @return the string containing the validation messages
     */
    public static String outputErrors(List<? extends Failure> failures) {
        return output(failures, TYPE.ERROR);
    }

    /**
     * Sorts and writes the list of warning messages to a string.
     *
     * @param failures the collection of failures to write
     * @return the string containing the validation messages
     */
    public static String outputWarnings(List<? extends Failure> failures) {
        return output(failures, TYPE.WARNING);
    }

    /**
     * Sorts and writes the list of errors to the given writer.
     *
     * @param writer   the writer
     * @param failures the collection of failures to write
     */
    public static void writeErrors(PrintWriter writer, List<? extends Failure> failures) {
        write(writer, failures, TYPE.ERROR);
    }

    /**
     * Sorts and writes the list of warnings to the given writer.
     *
     * @param writer   the writer
     * @param failures the collection of failures to write
     */
    public static void writeWarnings(PrintWriter writer, List<? extends Failure> failures) {
        write(writer, failures, TYPE.WARNING);
    }

    private static String output(List<? extends Failure> failures, TYPE type) {
        ByteArrayOutputStream bas = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(bas);
        write(writer, failures, type);
        return bas.toString();
    }

    private static void write(PrintWriter writer, List<? extends Failure> failures, TYPE type) {
        int count = 0;
        List<Failure> sorted = new ArrayList<>(failures);
        // sort the errors so that ArtifactValidationFailures are evaluated last. This is done so that nested failures are printed after all
        // failures in the parent artifact.
        Collections.sort(sorted, COMPARATOR);
        // if a composite is used multiple times, only report errors contained in it once
        HashSet<String> reported = new HashSet<>();
        for (Failure failure : sorted) {
            count = writerError(failure, writer, count, type, reported);
        }
        if (count == 1) {
            if (type == TYPE.ERROR) {
                writer.write("1 error was found \n\n");
            } else {
                writer.write("1 warning was found \n\n");
            }
        } else {
            if (type == TYPE.ERROR) {
                writer.write(count + " errors were found \n\n");
            } else {
                if (count != 0) {
                    writer.write(count + " warnings were found \n\n");
                }
            }
        }
        writer.flush();
    }

    private static int writerError(Failure failure, PrintWriter writer, int count, TYPE type, HashSet<String> reported) {
        if (failure instanceof ArtifactValidationFailure) {
            ArtifactValidationFailure artifactFailure = (ArtifactValidationFailure) failure;
            if (reported.contains(artifactFailure.getArtifactName())) {
                // if the error has already been reported because the artifact is used multiple times, don't print it again
                return count;
            }
            if (!errorsOnlyInContainedArtifacts(artifactFailure)) {
                if (type == TYPE.ERROR) {
                    writer.write("Errors in " + artifactFailure.getArtifactName() + " (" + artifactFailure.getContributionUri() + ")\n\n");
                } else {
                    writer.write("Warnings in " + artifactFailure.getArtifactName() + " (" + artifactFailure.getContributionUri() + ")\n\n");
                }
            }
            for (ValidationFailure childFailure : artifactFailure.getFailures()) {
                count = writerError(childFailure, writer, count, type, reported);
            }
            reported.add(artifactFailure.getArtifactName());
        } else {
            if (type == TYPE.ERROR) {
                writer.write("  ERROR: " + failure.getMessage() + "\n\n");
            } else {
                writer.write("  WARNING: " + failure.getMessage() + "\n\n");
            }
            ++count;
        }
        return count;
    }

    private static boolean errorsOnlyInContainedArtifacts(ArtifactValidationFailure artifactFailure) {
        for (ValidationFailure failure : artifactFailure.getFailures()) {
            if (!(failure instanceof ArtifactValidationFailure)) {
                return false;
            }
        }
        return true;
    }

}
