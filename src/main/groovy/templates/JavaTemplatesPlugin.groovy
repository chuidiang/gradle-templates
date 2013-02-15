package templates

import templates.tasks.CreateJavaProject

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

/**
 * Adds basic tasks for bootstrapping Java projects. Adds createJavaClass, createJavaProject,
 * createJavaSubProject, and initJavaProject tasks.
 */
class JavaTemplatesPlugin implements Plugin<Project> {
  static final String CREATE_JAVA_PROJECT_TASK_NAME = "createJavaProject"
  static final String CREATE_JAVA_SUBPROJECT_TASK_NAME = "createJavaSubProject"
  static final String CREATE_JAVA_CLASS_TASK_NAME = "createJavaClass"

  void apply(Project project) {
    // configureInitJavaProject(project)
    configureCreateJavaProject(project)
    // configureCreateJavaSubProject(project)
    // configureCreateJavaClass(project)
  }

  def configureInitJavaProject(project) { 
    Task initJavaProject = project.tasks.add(INIT_JAVA_PROJECT_TASK_NAME, InitJavaProject)
    initJavaProject.group = TemplatesPlugin.GROUP
    initJavaProject.description = 'Initializes a new Gradle Java project in the current directory.'
    createBase()
    File buildFile = new File('build.gradle')
    buildFile.exists() ?: buildFile.createNewFile()
    TemplatesPlugin.prependPlugin 'java', buildFile
  }

  def configureCreateJavaProject(project) { 
    Task createJavaProject =  project.tasks.add(CREATE_JAVA_PROJECT_TASK_NAME, CreateJavaProject)
    createJavaProject.group = TemplatesPlugin.GROUP
    createJavaProject.description = 'Creates a new Gradle Java project in a new directory named after your project.'
    createJavaProject.conventionMapping.startingDir = { project.rootDir }
  }

  def configureCreateJavaSubProject(project) { 
    Task createJavaSubProject =  project.tasks.add(CREATE_JAVA_SUBPROJECT_TASK_NAME, CreateJavaSubProject)
    createJavaSubProject.group = TemplatesPlugin.GROUP
    createJavaSubProject.description = 'Creates a new Gradle Java project in a new directory named after your project.'
  }

  def configureCreateJavaClass(project) { 
    Task createJavaClass = project.tasks.add(CREATE_JAVA_CLASS_TASK_NAME, CreateJavaClass)
    createJavaClass.group = TemplatesPlugin.GROUP
    createJavaClass.description = 'Creates a new Java class in the current project.'
    def mainSrcDir = null
    try {
      // get main java dir, and check to see if Java plugin is installed.
      mainSrcDir = findMainJavaDir(project)
    } catch (Exception e) {
      throw new IllegalStateException('It seems that the Java plugin is not installed, I cannot determine the main java source directory.', e)
    }

    def fullClassName = props['newClassName'] ?: TemplatesPlugin.prompt('Class name (com.example.MyClass)')
    if (fullClassName) {
      def classParts = JavaTemplatesPlugin.getClassParts(fullClassName)
      ProjectTemplate.fromUserDir {
	"${mainSrcDir}" {
	  "${classParts.classPackagePath}" {
	    "${classParts.className}.java" template: '/templates/java/java-class.tmpl',
	  classPackage: classParts.classPackage,
	  className: classParts.className
	  }
	}
      }
    } else {
      println 'No class name provided.'
    }
  }
}