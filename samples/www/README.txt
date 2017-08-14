Sample grading.xml and security.policy files for a network assignment
that uses jsoup to parse HTML.

The security.policy file is fairly complex to allow use of SSL/TLS
libraries while communicating with HTTPS websites. A number of
FilePermission policy entries are Mac-specified and need to be fixed
to operate on Windows. The URLPermission entries in the middle of the
policy file must be adjusted to allow access to different websites.

