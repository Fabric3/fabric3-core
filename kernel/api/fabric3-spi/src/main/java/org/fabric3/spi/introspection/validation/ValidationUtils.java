/*
* Fabric3
* Copyright (c) 2009-2012 Metaform Systems
*
* Fabric3 is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as
* published by the Free Software Foundation, either version 3 of
* the License, or (at your option) any later version, with the
* following exception:
*
* Linking this software statically or dynamically with other
* modules is making a combined work based on this software.
* Thus, the terms and conditions of the GNU General Public
* License cover the whole combination.
*
* As a special exception, the copyright holders of this software
* give you permission to link this software with independent
* modules to produce an executable, regardless of the license
* terms of these independent modules, and to copy and distribute
* the resulting executable under terms of your choice, provided
* that you also meet, for each linked independent module, the
* terms and conditions of the license of that module. An
* independent module is a module which is not derived from or
* based on this software. If you modify this software, you may
* extend this exception to your version of the software, but
* you are not obligated to do so. If you do not wish to do so,
* delete this exception statement from your version.
*
* Fabric3 is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty
* of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
* See the GNU General Public License for more details.
*
* You should have received a copy of the
* GNU General Public License along with Fabric3.
* If not, see <http://www.gnu.org/licenses/>.
*/
package org.fabric3.spi.introspection.validation;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.fabric3.host.contribution.ArtifactValidationFailure;
import org.fabric3.host.contribution.ValidationFailure;

/**
 * Utility methods for outputting validation errors.
 *
 * @version $Rev$ $Date$
 */
public final class ValidationUtils {
    private static ValidationExceptionComparator COMPARATOR = new ValidationExceptionComparator();

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
    public static String outputErrors(List<ValidationFailure> failures) {
        return output(failures, TYPE.ERROR);
    }

    /**
     * Sorts and writes the list of warning messages to a string.
     *
     * @param failures the collection of failures to write
     * @return the string containing the validation messages
     */
    public static String outputWarnings(List<ValidationFailure> failures) {
        return output(failures, TYPE.WARNING);
    }

    /**
     * Sorts and writes the list of errors to the given writer.
     *
     * @param writer   the writer
     * @param failures the collection of failures to write
     */
    public static void writeErrors(PrintWriter writer, List<ValidationFailure> failures) {
        write(writer, failures, TYPE.ERROR);
    }

    /**
     * Sorts and writes the list of warnings to the given writer.
     *
     * @param writer   the writer
     * @param failures the collection of failures to write
     */
    public static void writeWarnings(PrintWriter writer, List<ValidationFailure> failures) {
        write(writer, failures, TYPE.WARNING);
    }

    private static String output(List<ValidationFailure> failures, TYPE type) {
        ByteArrayOutputStream bas = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(bas);
        write(writer, failures, type);
        return bas.toString();
    }

    private static void write(PrintWriter writer, List<ValidationFailure> failures, TYPE type) {
        int count = 0;
        List<ValidationFailure> sorted = new ArrayList<ValidationFailure>(failures);
        // sort the errors so that ArtifactValidationFailures are evaluated last. This is done so that nested failures are printed after all
        // failures in the parent artifact.
        Collections.sort(sorted, COMPARATOR);
        // if a composite is used multiple times, only report errors contained in it once
        HashSet<String> reported = new HashSet<String>();
        for (ValidationFailure failure : sorted) {
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

    private static int writerError(ValidationFailure failure, PrintWriter writer, int count, TYPE type, HashSet<String> reported) {
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
