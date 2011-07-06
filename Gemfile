# Testing bundler output more.
Bundler.ui.error "++ Starting bundle"

ROOT_DIR=File.expand_path(File.dirname(__FILE__))
# Set home directory to this app. Needed for ivy dep resolution.
ENV['HOME'] = ROOT_DIR

# Set process working dir to root, bundle commands rely on being here
Dir.chdir ROOT_DIR

# This lets is dump a commands output through bundler's UI methods
# There is some weird output sync going on in the play commands - the banner comes last.
# Doesn't seem to be working thought.
def run_cmd(cmd)
  # Dump stderr to stdout, easier than using open3
  cmd = "bash -c '" + cmd + "' 2>&1 | tee last_bundle.log"
  IO.popen(cmd) do |f|
    until f.eof?
      Bundler.ui.info f.gets
    end
  end
end

# Synd dependencies
run_cmd('cd app && ../play-1.2.2/play dependencies --sync')
# Precompile our app
run_cmd("rm -r app/precompiled")
run_cmd("play-1.2.2/play precompile app")
