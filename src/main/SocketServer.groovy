class SocketServer {
	
	public static final long MEGABYTE = 1024 * 1024
	public static final int UDP_MAX = 65507
	public static final int TCP_PORT = 13267
	public static final int UDP_PORT = 12345
	
	static void main(String[] args) throws IOException {
		BufferedReader input = System.in.newReader()
		println("1. TCP.\n" +
				"2. UDP.\n" +
				"0. Sair.")
		int protocolo = input.readLine().toInteger()
		while (protocolo != 0) {
			switch (protocolo) {
				case 1:
					socketTCP(input)
					break
				case 2:
					socketUDP(input)
					break
				default:
					println("Opção inválida. Escolha novamente.")
			}
			
			println("1. TCP.\n" +
					"2. UDP.\n" +
					"0. Sair.")
			protocolo = input.readLine().toInteger()
		}
	}
	
	static void socketTCP(final BufferedReader input = System.in.newReader()) throws IOException {
		BufferedInputStream inputStream
		OutputStream outputStream
		String caminhoDoArquivo
		ServerSocket serverSocket
		Socket socket
		byte[] bytes
		
		try {
			serverSocket = new ServerSocket(TCP_PORT)
			
			println("1. Escolher arquivo para enviar.\n" +
					"0. Sair")
			boolean continuar = input.readLine().toInteger() as boolean
			
			while (continuar) {
				try {
					println("Insira o caminho do arquivo (de até 5MB): ")
					caminhoDoArquivo = input.readLine().replaceAll(/([^(\\{1,2}).+]?)\\{1,2}([^(\\{1,2}).+]?)/, /$1\/$2/)
					File file = new File(caminhoDoArquivo)
					while (!file.exists() || file.length() > 5 * MEGABYTE) {
						println("Arquivo inexistente ou tamanho maior que o limite.\n" +
								"Insira um caminho de um arquivo válido: ")
						caminhoDoArquivo = input.readLine()
						file = new File(caminhoDoArquivo)
					}
					
					println("Conectando...")
					socket = serverSocket.accept()
					println("Conectado.")
					
					// envia nome do arquivo
					ObjectOutputStream saida = new ObjectOutputStream(socket.getOutputStream())
					saida.flush()
					saida.writeUTF(caminhoDoArquivo.find(/[A-Za-z0-9_\-\.]+\.[A-Za-z0-9]+\u0024/))
					saida.close()
					
					// envia arquivo
					bytes = new byte[(int) file.length()]
					FileInputStream fis = new FileInputStream(file)
					inputStream = new BufferedInputStream(fis)
					inputStream.read(bytes, 0, bytes.length)
					println("Conectando...")
					socket = serverSocket.accept()
					println("Conectado.")
					outputStream = socket.getOutputStream()
					println("Enviando $caminhoDoArquivo (${bytes.length} bytes)")
					outputStream.write(bytes, 0, bytes.length)
					outputStream.flush()
					inputStream.close()
					outputStream.close()
					
				} catch (Exception ex) {
					System.err.println("Impossível enviar o arquivo.")
					ex.printStackTrace()
				}
				println("Feito.")
				
				println("1. Escolher arquivo para enviar.\n" +
						"0. Sair")
				continuar = input.readLine().toInteger() as boolean
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
			if (socket && !socket.isClosed()) {
				socket.close()
			}
		}
	}
	
	static void socketUDP(final BufferedReader input = System.in.newReader()) throws IOException {
		DatagramSocket datagramSocket
		DatagramPacket datagramPacket
		InetAddress ip = InetAddress.getLocalHost()
		BufferedInputStream inputStream
		OutputStream outputStream
		String caminhoDoArquivo
		byte[] bytes
		
		try {
			datagramSocket = new DatagramSocket()
			
			println("1. Escolher arquivo para enviar.\n" +
					"0. Sair")
			boolean continuar = input.readLine().toInteger() as boolean
			
			while (continuar) {
				try {
					println("Insira o caminho do arquivo (de até 5MB): ")
					caminhoDoArquivo = input.readLine().replaceAll(/([^(\\{1,2}).+]?)\\{1,2}([^(\\{1,2}).+]?)/, /$1\/$2/)
					File file = new File(caminhoDoArquivo)
					while (!file.exists() || file.length() > 5 * MEGABYTE) {
						println("Arquivo inexistente ou tamanho maior que o limite.\n" +
								"Insira um caminho de um arquivo válido: ")
						caminhoDoArquivo = input.readLine()
						file = new File(caminhoDoArquivo)
					}
					byte[] bytesNomeArquivo = caminhoDoArquivo.find(/[A-Za-z0-9_\-\.]+\.[A-Za-z0-9]+\u0024/)
							.getBytes()
					println("Conectando...")
					datagramPacket = new DatagramPacket(bytesNomeArquivo, bytesNomeArquivo.length, ip, UDP_PORT)
					println("Conectado.")
					
					// envia nome do arquivo
					datagramSocket.send(datagramPacket)
					
					// envia arquivo
					bytes = new byte[(int) file.length()]
					FileInputStream fis = new FileInputStream(file)
					inputStream = new BufferedInputStream(fis)
					inputStream.read(bytes, 0, bytes.length)
					
					// TODO Particionar arquivo
					if (bytes.length > UDP_MAX) {
						List<byte[]> partitions = new ArrayList<byte[]>()
						for (int i = 0; i < bytes.length; i += UDP_MAX) {
							partitions.add(Arrays.copyOfRange(bytes, i, Math.min(i + UDP_MAX, bytes.length)))
						}
					} else {
						datagramPacket = new DatagramPacket(bytes, bytes.length, ip, UDP_PORT)
						println("Enviando $caminhoDoArquivo (${bytes.length} bytes)")
						datagramSocket.send(datagramPacket)
					}
				} catch (Exception ex) {
					System.err.println("Impossível enviar o arquivo.")
					ex.printStackTrace()
				}
				println("Feito.")
				
				println("1. Escolher arquivo para enviar.\n" +
						"0. Sair")
				continuar = input.readLine().toInteger() as boolean
			}
		}
		finally {
			if (inputStream) {
				inputStream.close()
			}
			if (outputStream) {
				outputStream.close()
			}
			if (datagramSocket && !datagramSocket.isClosed()) {
				datagramSocket.close()
			}
		}
	}
}
