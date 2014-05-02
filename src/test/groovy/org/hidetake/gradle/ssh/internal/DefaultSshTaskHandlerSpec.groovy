package org.hidetake.gradle.ssh.internal

import org.hidetake.gradle.ssh.internal.session.Sessions
import org.hidetake.gradle.ssh.plugin.Remote
import spock.lang.Specification
import spock.lang.Unroll
import spock.util.mop.ConfineMetaClassChanges

import static org.hidetake.gradle.ssh.test.RegistryHelper.factoryOf

@ConfineMetaClassChanges(Sessions)
class DefaultSshTaskHandlerSpec extends Specification {

    DefaultSshTaskHandler sshTaskDelegate
    Sessions sessions

    def setup() {
        sessions = Mock(Sessions)
        factoryOf(Sessions) << Mock(Sessions.Factory) {
            create() >> sessions
        }
        sshTaskDelegate = new DefaultSshTaskHandler()
    }

    def "add a session"() {
        given:
        def remote = new Remote('myRemote')
        remote.user = 'myUser'
        remote.host = 'myHost'
        def operationClosure = { assert false }

        when:
        sshTaskDelegate.session(remote, operationClosure)

        then:
        1 * sessions.add(remote, operationClosure)
    }

    def "add a session with null remote throws assertion error"() {
        given:
        def operationClosure = { assert false }

        when:
        sshTaskDelegate.session(null as Remote, operationClosure)

        then:
        AssertionError e = thrown()
        e.message.contains('remote')
    }

    @Unroll
    def "add session with invalid params throws assertion error"() {
        given:
        def remote = new Remote('myRemote')
        remote.host = theHost

        when:
        sshTaskDelegate.session(remote, theOperationClosure)

        then:
        AssertionError e = thrown()
        e.message.contains(errorContains)

        where:
        theHost          | theOperationClosure | errorContains
        null             | { assert false }    | "host"
        "www.myhost.com" | null                | "closure"
    }



    def "add session for multiple remotes"() {
        given:
        def remote1 = new Remote('myRemote1')
        remote1.user = 'myUser1'
        remote1.host = 'myHost1'
        def remote2 = new Remote('myRemote2')
        remote2.user = 'myUser2'
        remote2.host = 'myHost2'
        def closure = { assert false }

        when:
        sshTaskDelegate.session([remote1, remote2], closure)

        then:
        1 * sessions.add(remote1, closure)
        1 * sessions.add(remote2, closure)
    }

    def "add session for empty remotes throws assertion error"() {
        given:
        def closure = { assert false }

        when:
        sshTaskDelegate.session([], closure)

        then:
        AssertionError ex = thrown()
        ex.message.contains("remotes")
    }

    def "add session for multiple remotes with null closure throws assertion error"() {
        given:
        def remote = new Remote('myRemote')
        remote.user = 'myUser'
        remote.host = 'myHost'

        when:
        sshTaskDelegate.session([remote], null)

        then:
        AssertionError ex2 = thrown()
        ex2.message.contains("closure")
    }

}
