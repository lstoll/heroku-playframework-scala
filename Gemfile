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
  cmd = "bash -c '" + cmd + "' 2>&1 | tee -a last_bundle.log"
  IO.popen(cmd) do |f|
    until f.eof?
      Bundler.ui.info f.gets
    end
  end
end

ivy_cache_dir = '/tmp/ivy_cache_' +  Time.now.to_i.to_s

# Remove log file
run_cmd('rm last_bundle.log')
# Sync dependencies
run_cmd('cd app && ../play-1.2.2/play dependencies -Divy.cache.dir=' + ivy_cache_dir + ' --sync')
# Remove ivy cache contents
run_cmd('rm -rf ' + ivy_cache_dir)
run_cmd('cd app && ../play-1.2.2/play dependencies -Divy.cache.dir=' + ivy_cache_dir + ' --sync')
# Precompile our app
run_cmd("rm -r app/precompiled")
run_cmd("play-1.2.2/play precompile app")
