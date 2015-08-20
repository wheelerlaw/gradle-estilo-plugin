/**
 * Copyright 2015 Anshul Verma. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.anshulverma.gradle.estilo

import groovy.util.logging.Slf4j
import net.anshulverma.gradle.estilo.checkstyle.CheckPropertiesExtension
import net.anshulverma.gradle.estilo.checkstyle.checks.CheckType
import net.anshulverma.gradle.estilo.util.AbstractDSL

/**
 * @author Anshul Verma (anshul.verma86@gmail.com)
 */
@Slf4j
class EstiloExtension extends AbstractDSL<Closure> {

  CheckType baseChecks
  List checks = []

  def source(String type) {
    baseChecks = CheckType.valueOf(type.toUpperCase())
  }

  @Override
  protected handle(String name, Closure closure) {
    log.debug('adding custom check {}', name)
    def check = new CheckPropertiesExtension(name)
    closure.@owner = check
    closure()
    checks.push(check)
  }

  @Override
  protected handle(String name) {
    throw new MissingPropertyException('cannot retrieve arbitrary properties', name, this.class)
  }
}
