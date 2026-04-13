Feature: Authentication and Authorization

  # Scenario 1 - unregistered email
  Scenario: Login with an unregistered email returns 401
    When I POST to "/auth/login" with body:
      """
      {"email": "ghost@example.com", "password": "somepassword1!"}
      """
    Then the response status should be 401
    And the response should contain error "Invalid credentials"

  # Scenario 2 - wrong password
  Scenario: Login with a registered email but wrong password returns 401
    Given a user is registered with email "alice@example.com" and password "correctPass1!"
    When I POST to "/auth/login" with body:
      """
      {"email": "alice@example.com", "password": "wrongPassword1!"}
      """
    Then the response status should be 401
    And the response should contain error "Invalid credentials"

  # Scenario 3 - no token on protected endpoint
  Scenario: Accessing a protected endpoint without a token returns 401
    When I GET "/projects" without a token
    Then the response status should be 401

  # Scenario 4 - duplicate email
  Scenario: Registering with an already-registered email returns 409
    Given a user is registered with email "bob@example.com" and password "password123!"
    When I POST to "/auth/register" with body:
      """
      {"name": "Bob Again", "email": "bob@example.com", "password": "differentPass1!"}
      """
    Then the response status should be 409
    And the response should contain error "Email already registered"

  # Scenario 5 - malformed registration data
  Scenario: Registering with malformed data returns 400
    When I POST to "/auth/register" with body:
      """
      {"name": "", "email": "not-an-email", "password": "short"}
      """
    Then the response status should be 400
    And the response error field should be "validation failed"

  # Scenario 6 - expired JWT
  Scenario: Accessing a protected endpoint with an expired JWT returns 401
    When I GET "/projects" with an expired JWT
    Then the response status should be 401

  # Scenario 7 - wrong signing secret
  Scenario: Accessing a protected endpoint with a JWT signed by a wrong secret returns 401
    When I GET "/projects" with a JWT signed by a wrong secret
    Then the response status should be 401

  # Scenario 8 - valid JWT
  Scenario: Accessing a protected endpoint with a valid JWT returns 200
    Given a user is registered with email "carol@example.com" and password "validPass1!"
    And I log in with email "carol@example.com" and password "validPass1!"
    When I GET "/projects" with the obtained token
    Then the response status should be 200

  # Scenario 9 - accessing another user's project returns 403
  Scenario: Accessing a project you do not own or have tasks in returns 403
    Given a user is registered with email "owner@example.com" and password "ownerPass1!"
    And I log in with email "owner@example.com" and password "ownerPass1!"
    And I create a project named "Owner's Private Project"
    Given a user is registered with email "intruder@example.com" and password "intruderPass1!"
    And I log in with email "intruder@example.com" and password "intruderPass1!"
    When I GET the previously created project with the obtained token
    Then the response status should be 403
