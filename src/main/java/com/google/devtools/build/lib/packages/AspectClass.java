// Copyright 2015 The Bazel Authors. All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.devtools.build.lib.packages;

/**
 * A class of aspects.
 *
 * <p>An aspect allows a rule to create actions in its dependencies, without their knowledge. It can
 * be viewed as the ability to attach shadow targets to transitive dependencies or a way to run
 * visitations of certain parts of the transitive closure of a rule in such a way that can be cached
 * (even partially) and reused between different configured targets requiring the same aspect. Some
 * examples where aspects are useful:
 *
 * <ul>
 *   <li>Converting the .jar files in the transitive closure of an Android binary to dexes
 *   <li>Emitting Java sources for a <code>proto_library</code> and the messages it depends on
 *   <li>Collecting all the dependencies of a rule to make sure that it does not contain a forbidden
 *       one
 * </ul>
 *
 * <p>When a configured target requests that an aspect be attached to one of its dependencies, the
 * {@link com.google.devtools.build.lib.analysis.TransitiveInfoProvider}s generated by that aspects
 * are merged with those of the actual dependency, that is, {@link
 * com.google.devtools.build.lib.analysis.RuleContext#getPrerequisite( String,
 * RuleConfiguredTarget.Mode)} will contain the transitive info providers produced both by the
 * dependency and the aspects that are attached to it.
 *
 * <p>Configured targets can specify which aspects should be attached to some of their dependencies
 * by specifying this in their {@link com.google.devtools.build.lib.analysis.RuleDefinition}: each
 * attribute can have a list of aspects to be applied to the rules in that attribute and each aspect
 * can specify which {@link com.google.devtools.build.lib.analysis.TransitiveInfoProvider}s it needs
 * on a rule so that it can do meaningful work (for example, dexing only makes sense for configured
 * targets that produce Java code).
 *
 * <p>Aspects can be defined natively, in Java ({@link NativeAspectClass}) or in Starlark ({@link
 * StarlarkAspectClass}).
 *
 * <p>Bazel propagates aspects through a multistage process. The general pipeline is as follows:
 *
 * <pre>
 *  {@link AspectClass}
 *   |
 *   V
 *  {@code AspectDescriptor} <- {@link AspectParameters}, {@code inheritedRequiredProviders},
 *   \                          {@code inheritedAttributeAspects}
 *   \
 *   V
 *  {@link Aspect} <- {@link AspectDefinition} (might require loading Starlark files)
 *   |
 *   V
 *  {@code ConfiguredAspect}  <- {@code ConfiguredTarget}
 *  </pre>
 *
 * <ul>
 *   <li>{@link AspectClass} is a moniker for "user" definition of the aspect, be it a native aspect
 *       or a Starlark aspect. It contains either a reference to the native class implementing the
 *       aspect or the location of the Starlark definition of the aspect in the source tree, i.e.
 *       label of .bzl file + symbol name.
 *   <li>{@link AspectParameters} is a (key,value) pair list that can be used to parameterize aspect
 *       classes
 *   <li>{@link AspectDescriptor} is a wrapper for {@code AspectClass}, {@link AspectParameters},
 *       {@code inheritedRequiredProviders} and {@code inheritedAttributeAspects}. It uniquely
 *       identifies the aspect and can be used in SkyKeys.
 *   <li>{@link AspectDefinition} is a class encapsulating the aspect definition (what attributes
 *       aspoect has, and along which dependencies does it propagate.
 *   <li>{@link Aspect} is a fully instantiated instance of an Aspect after it is loaded. Getting an
 *       {@code Aspect} from {@code AspectDescriptor} for Starlark aspects requires adding a
 *       Skyframe dependency.
 *   <li>{@link com.google.devtools.build.lib.analysis.ConfiguredAspect} represents a result of
 *       application of an {@link Aspect} to a given {@link
 *       com.google.devtools.build.lib.analysis.ConfiguredTarget}.
 * </ul>
 *
 * {@link AspectDescriptor}, or in general, a tuple of ({@link AspectClass}, {@link
 * AspectParameters}), {@code inheritedRequiredProviders} and {@code inheritedAttributeAspects} is
 * an identifier that should be used in SkyKeys or in other contexts that need equality for aspects.
 * See also {@link com.google.devtools.build.lib.skyframe.AspectFunction} for details on Skyframe
 * treatment of Aspects.
 *
 * @see com.google.devtools.build.lib.analysis.RuleConfiguredTargetFactory
 * @see com.google.devtools.build.lib.skyframe.AspectFunction
 */
public interface AspectClass {
  /**
   * Returns an aspect name.
   */
  String getName();

  default String getKey() {
    return getName();
  }
}
