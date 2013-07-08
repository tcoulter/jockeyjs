#
# Be sure to run `pod spec lint JockeyJS.podspec' to ensure this is a
# valid spec and remove all comments before submitting the spec.
#
# To learn more about the attributes see http://docs.cocoapods.org/specification.html
#
Pod::Spec.new do |s|
  s.name         = "JockeyJS"
  s.version      = "1.0.0"
  s.license      = 'MIT'
  s.summary      = "JockeyJS is an iOS and JS library that facilitates two-way communication between iOS apps and JavaScript apps running inside them."
  s.homepage     = "https://github.com/tcoulter/jockeyjs"
  s.author       = { "Tim Coulter" => "tim@timothyjcoulter.com" }
  s.source       = { :git => "https://github.com/tcoulter/jockeyjs.git", :tag => "1.0.0" }
  s.source_files = 'JockeyJS/includes/Jockey.{h,m}'
  s.requires_arc = true
  s.platform = :ios
end
