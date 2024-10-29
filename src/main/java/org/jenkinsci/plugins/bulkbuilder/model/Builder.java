/*
 * The MIT License
 *
 * Copyright (c) 2010-2011 Simon Westcott
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.jenkinsci.plugins.bulkbuilder.model;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import hudson.Util;
import hudson.model.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * @author simon
 */
public class Builder {

    private static final Logger LOGGER = Logger.getLogger(Builder.class.getName());

    private BuildAction action;

    /**
     * Key/value map of user parameters
     */
    private Map<String, String> param;

    private String pattern;

    private String view;

    public Builder(BuildAction action) {
        this.action = action;
    }

    public void setUserParams(Map<String, String> param) {
        this.param = param;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public void setView(String view) {
        this.view = view;
    }

    /**
     * Build Jenkins projects
     */
    protected int build(ArrayList filters) {
        int i = 0;

        // Build composite predicate of all build prefs
        Predicate<AbstractProject<?, ?>> compositePredicate = Predicates.and(filters);

        List<AbstractProject<?, ?>> projects = getProjects(this.view);

        // Use composite predicate to identify target projects
        Iterable<AbstractProject<?, ?>> targetProjects = Iterables.filter(projects, compositePredicate);

        for (AbstractProject<?, ?> project : targetProjects) {
            LOGGER.log(Level.FINE, "Scheduling build for job '" + project.getDisplayName() + "'");
            if (performBuildProject(project)) {
                i++;
            }
        }

        return i;
    }

    private int buildWorseOrEqualsTo(final Result r) {
        LOGGER.log(Level.FINE, "Starting to build " + r.toString() + " jobs.");

        ArrayList<Predicate<AbstractProject<?, ?>>> filters = new ArrayList<Predicate<AbstractProject<?, ?>>>();

        filters.add(new Predicate<AbstractProject<?, ?>>() {
            @Override
            public boolean apply(AbstractProject<?, ?> project) {
                AbstractBuild<?, ?> build = project.getLastCompletedBuild();
                return build == null || build.getResult().isWorseOrEqualTo(r);
            }
        });

        filters = addSubFilters(filters);

        int i = build(filters);

        LOGGER.log(Level.FINE, "Finished building " + r.toString() + " jobs.");

        return i;
    }

    private int buildExactStatus(final Result r) {
        LOGGER.log(Level.FINE, "Starting to build " + r.toString() + " jobs.");

        ArrayList<Predicate<AbstractProject<?, ?>>> filters = new ArrayList<Predicate<AbstractProject<?, ?>>>();

        filters.add(new Predicate<AbstractProject<?, ?>>() {
            @Override
            public boolean apply(AbstractProject<?, ?> project) {
                AbstractBuild<?, ?> build = project.getLastCompletedBuild();
                return build != null && build.getResult() == r;
            }
        });

        filters = addSubFilters(filters);

        int i = build(filters);

        LOGGER.log(Level.FINE, "Finished building " + r.toString() + " jobs.");

        return i;
    }

    private ArrayList addSubFilters(ArrayList filters) {
        if (this.pattern != null) {
            Predicate<AbstractProject<?, ?>> patternPred = new Predicate<AbstractProject<?, ?>>() {
                @Override
                public boolean apply(AbstractProject<?, ?> project) {
                    String patternReg = Builder.this.pattern.replaceAll("\\*", "\\.\\*");
                    return Pattern.matches(patternReg, project.getDisplayName());
                }
            };
            filters.add(patternPred);
        }

        return filters;
    }

    /**
     * Build all Jenkins projects
     */
    public final int buildAll() {
        return buildWorseOrEqualsTo(Result.SUCCESS);
    }

    /**
     * Build all unstable builds.
     *
     * This includes projects that are unstable, have not been built before, failed
     * and aborted projects.
     */
    public final int buildUnstable() {
        return buildWorseOrEqualsTo(Result.UNSTABLE);
    }

    /**
     * Build all unstable builds only.
     */
    public final int buildUnstableOnly() {
        return buildExactStatus(Result.UNSTABLE);
    }

    /**
     * Build failed Jenkins projects.
     *
     * This includes projects that have not been built before and failed and aborted
     * projects.
     */
    public final int buildFailed() {
        return buildWorseOrEqualsTo(Result.FAILURE);
    }

    /**
     * Build all failed builds only.
     */
    public int buildFailedOnly() {
        return buildExactStatus(Result.FAILURE);
    }

    /**
     * Build all not built jobs.
     *
     * This includes projects that are have not been built before and aborted
     * projects.
     */
    public int buildNotBuilt() {
        return buildWorseOrEqualsTo(Result.NOT_BUILT);
    }

    /**
     * Build all not built jobs only.
     */
    public int buildNotBuildOnly() {
        return buildExactStatus(Result.NOT_BUILT);
    }

    /**
     * Build all aborted builds.
     */
    public int buildAborted() {
        return buildWorseOrEqualsTo(Result.ABORTED);
    }

    /**
     * Return a list of projects which can be built
     *
     * @return
     */
    protected final List<AbstractProject<?, ?>> getProjects(String viewName) {

        List<AbstractProject<?, ?>> projects = new ArrayList<AbstractProject<?, ?>>();
        Collection<TopLevelItem> items = Hudson.getInstance().getItems();

        if (viewName != null) {
            View view = Hudson.getInstance().getView(viewName);

            if (view != null) {
                items = view.getItems();
            }
        }

         for (Job<?, ?> project : Util.createSubList(items, Job.class)) {
            if (!project.isBuildable()) {
                continue;
            }
            projects.add(project);
        }

        return projects;
    }

    /**
     * Actually build a project, passing in parameters where appropriate
     *
     * @param project
     * @return
     */
    protected final boolean performBuildProject(AbstractProject<?, ?> project) {
        if (!project.hasPermission(AbstractProject.BUILD)) {
            LOGGER.log(Level.WARNING, "Insufficient permission to build job '" + project.getName() + "'");
            return false;
        }

        if (action.equals(BuildAction.POLL_SCM)) {
            project.schedulePolling();
            return true;
        }

        // no user parameters provided, just build it
        if (param == null) {
            project.scheduleBuild(new Cause.UserCause());
            return true;
        }

        ParametersDefinitionProperty pp = (ParametersDefinitionProperty) project
                .getProperty(ParametersDefinitionProperty.class);

        // project does not except any parameters, just build it
        if (pp == null) {
            project.scheduleBuild(new Cause.UserCause());
            return true;
        }

        List<ParameterDefinition> parameterDefinitions = pp.getParameterDefinitions();
        List<ParameterValue> values = new ArrayList<ParameterValue>();

        for (ParameterDefinition paramDef : parameterDefinitions) {
            ParameterValue value;

            // Check if user supplied the parameter. If not, then use the default value.
            if (paramDef instanceof BooleanParameterDefinition) {
                BooleanParameterDefinition boolParaDef = (BooleanParameterDefinition) paramDef;
                // Did user supply this parameter?
                if (this.userParams.containsKey(paramDef.getName())) {
                    value = boolParaDef.createValue(this.userParams.get(boolParaDef.getName()));
                } else {
                    value = boolParaDef.getDefaultParameterValue();
                }
            } else if (paramDef instanceof ChoiceParameterDefinition) {
                ChoiceParameterDefinition choiceParaDef = (ChoiceParameterDefinition) paramDef;
                // Did user supply this parameter?
                if (this.userParams.containsKey(paramDef.getName())) {
                    value = choiceParaDef.createValue(this.userParams.get(choiceParaDef.getName()));
                } else {
                    value = choiceParaDef.createValue(choiceParaDef.getDefaultParameterValue().value);
                }
            } else if (paramDef instanceof StringParameterDefinition) {
                StringParameterDefinition stringParamDef = (StringParameterDefinition) paramDef;
                if (userParams.containsKey(paramDef.getName())) {
                    value = stringParamDef.createValue(userParams.get(stringParamDef.getName()));
                } else {
                    value = stringParamDef.createValue(stringParamDef.getDefaultValue());
                }
            } else {
                // TODO add support for other parameter types
                value = paramDef.getDefaultParameterValue();
                continue;
            }
            LOGGER.log(Level.INFO, "Parameter Value = " + value.toString());
            values.add(value);
        }

        Hudson.getInstance().getQueue().schedule(pp.getOwner(), 1, new ParametersAction(values));
        return true;
    }

}
