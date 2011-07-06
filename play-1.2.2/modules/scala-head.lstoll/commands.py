# Scala
import sys
import inspect
import os
import subprocess
import shutil

from play.utils import *

MODULE = 'scala'

COMMANDS = ['scala:console']

def execute(**kargs):
    command = kargs.get("command")
    app = kargs.get("app")
    args = kargs.get("args")
    env = kargs.get("env")

    if command == "scala:console":
        # add precompiled classes to classpath
        cp_args = app.cp_args() + ":" + os.path.normpath(os.path.join(app.path,'tmp', 'classes'))
        # replace last element with the console app
        java_cmd = app.java_cmd(args, cp_args)
        java_cmd[len(java_cmd)-2] = "play.console.Console"
        java_cmd.insert(2, '-Xmx512M')
        subprocess.call(java_cmd, env=os.environ)
        print

def before(**kargs):
    command = kargs.get("command")
    app = kargs.get("app")
    args = kargs.get("args")
    env = kargs.get("env")

    if command == 'run' or command == 'test' or command == 'auto-test':
        # Only add JVM params if they aren't already set
        if len([p for p in args if '-Xmx' in p]) == 0:
            args.append('-Xms512m')
            args.append('-Xmx512m')
            args.append('-XX:PermSize=256m')
            args.append('-XX:MaxPermSize=256m')

def after(**kargs):

    command = kargs.get("command")
    app = kargs.get("app")
    args = kargs.get("args")
    env = kargs.get("env")

    # ~~~~~~~~~~~~~~~~~~~~~~ new
    if command == 'new':
        shutil.rmtree(os.path.join(app.path, 'app/controllers'))
        shutil.rmtree(os.path.join(app.path, 'app/models'))
        shutil.rmtree(os.path.join(app.path, 'app/views/errors'))
        os.remove(os.path.join(app.path, 'test/BasicTest.java'))
        os.remove(os.path.join(app.path, 'test/ApplicationTest.java'))
        os.remove(os.path.join(app.path, 'app/views/main.html'))
        os.remove(os.path.join(app.path, 'app/views/Application/index.html'))
        module_dir = inspect.getfile(inspect.currentframe()).replace("commands.py", "")
        shutil.copyfile(os.path.join(module_dir, 'resources', 'controllers.scala'), os.path.join(app.path, 'app', 'controllers.scala'))
        shutil.copyfile(os.path.join(module_dir, 'resources', 'Tests.scala'), os.path.join(app.path, 'test', 'Tests.scala'))
        shutil.copyfile(os.path.join(module_dir, 'resources', 'main.scala.html'), os.path.join(app.path, 'app/views', 'main.scala.html'))
        shutil.copyfile(os.path.join(module_dir, 'resources', 'index.scala.html'), os.path.join(app.path, 'app/views/Application', 'index.scala.html'))
        ac = open(os.path.join(app.path, 'conf/application.conf'), 'r')
        conf = ac.read()
        ac = open(os.path.join(app.path, 'conf/application.conf'), 'w')
        ac.write(conf)

    # ~~~~~~~~~~~~~~~~~~~~~~ Eclipsify
    if command == 'ec' or command == 'eclipsify':
        dotProject = os.path.join(app.path, '.project')
        replaceAll(dotProject, r'org.eclipse.jdt.core.javabuilder', "ch.epfl.lamp.sdt.core.scalabuilder")
        replaceAll(dotProject, r'<natures>', "<natures>\n\t\t<nature>ch.epfl.lamp.sdt.core.scalanature</nature>")

    # ~~~~~~~~~~~~~~~~~~~~~~ Idealize
    if command == 'idea' or command == 'idealize':
        application_name = app.readConf('application.name')
        imlFile = os.path.join(app.path, application_name + '.iml')
        replaceAll(imlFile, r'</module>', '''
    <component name="FacetManager">
        <facet type="scala" name="Scala">
            <configuration>
                <option name="compilerLibraryLevel" value="Global" />
            </configuration>
        </facet>
    </component>
</module>''')
