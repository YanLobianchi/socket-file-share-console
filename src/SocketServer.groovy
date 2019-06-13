class SocketServer {
	
	public static final long MEGABYTE = 1024*1024
	public final static int SOCKET_PORT = 13267
	
	static void main(String[] args) throws IOException {
		BufferedInputStream inputStream = null
		OutputStream outputStream = null
		ServerSocket serverSocket = null
		Socket socket = null
		def input = System.in.newReader()
		
		try {
			try {
				println("Conectando...")
				serverSocket = new ServerSocket(SOCKET_PORT)
				println("Conectado.")
			} catch (Exception ex) {
				System.err.println("Impossível estabelecer conexão com a porta.")
				ex.printStackTrace()
			}
			
			try {
				socket = serverSocket.accept()
			} catch (Exception ex) {
				System.err.println("Conexão não aceita pelo servidor.")
				ex.printStackTrace()
			}
			println("Conexão aceita: " + socket)
			
			println("1. Escolher arquivo para enviar.\n" +
					"2. Sair")
			int opcao = input.readLine().toInteger()
			while (opcao != 2) {
				if (opcao == 1) {
					byte[] bytes = new byte[5 * MEGABYTE]
					String caminhoDoArquivo = ""
					try {
						println("Insira o caminho do arquivo (de até 5MB): ")
						caminhoDoArquivo = input.readLine()
						File file = new File(caminhoDoArquivo)
						while (!file.exists() || file.length() > 5 * MEGABYTE) {
							println("Arquivo inexistente ou tamanho maior que o limite.\n" +
									"Insira um caminho de um arquivo válido: ")
							caminhoDoArquivo = input.readLine()
							file = new File(caminhoDoArquivo)
						}
						
						bytes = new byte[(int) file.length()]
						FileInputStream fis = new FileInputStream(file)
						inputStream = new BufferedInputStream(fis)
						inputStream.read(bytes, 0, bytes.length)
					} catch (Exception ex) {
						System.err.println("Impossível montar buffer do arquivo.")
						ex.printStackTrace()
					}
					
					try {
						outputStream = socket.getOutputStream()
						println("Enviando $caminhoDoArquivo (${bytes.length} bytes)")
						outputStream.write(bytes, 0, bytes.length)
						outputStream.flush()
					} catch (Exception ex) {
						System.err.println("Impossível enviar o arquivo.")
						ex.printStackTrace()
					}
					println("Feito.")
				}
				
				println("1. Escolher arquivo para enviar.\n" +
						"2. Sair")
				opcao = input.readLine().toInteger()
			}
		}
		finally {
			if (serverSocket) {
				serverSocket.close()
			}
			if (inputStream) {
				inputStream.close()
			}
			if (outputStream) {
				outputStream.close()
			}
			if (socket) {
				socket.close()
			}
		}
	}
}
