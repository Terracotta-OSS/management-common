/*
 * Copyright Terracotta, Inc.
 * Copyright IBM Corp. 2024, 2025
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
package org.terracotta.management.security.web.shiro;

import java.io.IOException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

import jakarta.servlet.http.HttpServletResponse;
import org.apache.shiro.web.filter.authz.PermissionsAuthorizationFilter;
import org.apache.shiro.web.util.WebUtils;

public class PermsFilter extends PermissionsAuthorizationFilter {
  @Override
  protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws IOException {
    if (!response.isCommitted()) {
      HttpServletResponse httpServletResponse = WebUtils.toHttp(response);
      httpServletResponse.resetBuffer();
      httpServletResponse.setStatus(403);
    }

    return false;
  }
}
