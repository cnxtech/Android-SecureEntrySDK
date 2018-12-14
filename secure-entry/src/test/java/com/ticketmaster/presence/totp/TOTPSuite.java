package com.ticketmaster.presence.totp;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        GeneratorTests.class,
        Sha1Tests.class,
        Sha256Tests.class,
        Sha512Tests.class,
        TOTPExceptionTests.class
})
public class TOTPSuite {
}
