/*
 * Copyright 2015 HM Revenue & Customs
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

package support

import java.io.File
import java.nio.file.Files

import org.apache.sshd.SshServer
import org.apache.sshd.common.NamedFactory
import org.apache.sshd.common.file.virtualfs.VirtualFileSystemFactory
import org.apache.sshd.server.auth.UserAuthNone
import org.apache.sshd.server.command.ScpCommandFactory
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider
import org.apache.sshd.server.{Command, UserAuth}
import org.apache.sshd.sftp.subsystem.SftpSubsystem

import scala.collection.JavaConversions._


object EmbeddedSFTPServer {

  private val sshd:SshServer = SshServer.setUpDefaultServer()

  def startServer(rootDir:File, port:Int = 2223): Unit = {

    val keyFile = Files.createTempFile("sftp", "sftp-private-key")

    sshd.setPort(port)
    sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(keyFile.toFile.getAbsolutePath))
    val userAuthFactories = new java.util.ArrayList[NamedFactory[UserAuth]]
    userAuthFactories.add(new UserAuthNone.Factory)
    sshd.setUserAuthFactories(userAuthFactories)
    sshd.setCommandFactory(new ScpCommandFactory)
    val fileSystemFactory = new VirtualFileSystemFactory(rootDir.getAbsolutePath)
    sshd.setFileSystemFactory(fileSystemFactory)
    val namedFactoryList = new java.util.ArrayList[NamedFactory[Command]]()
    namedFactoryList.add(new SftpSubsystem.Factory)

    sshd.setSubsystemFactories(namedFactoryList)

    sshd.start()
  }

  def stopServer() {
    sshd.getActiveSessions().toList.foreach(_.disconnect(1, "Done"))
    sshd.stop(true)
  }


}
