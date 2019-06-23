import java.nio.file.Files
import java.nio.file.Paths

class SocketClient {

	public final static int TCP_PORT = 13267
	public final static int UDP_PORT = 12345
	public final static String SERVER = "127.0.0.1"
	public final static String DESKTOP_PATH = "${System.getProperty("user.home")}/Desktop/"
	public final static int FILE_SIZE = 5 * 1024 * 1024
	public final static int FILE_SIZE_UDP = 65507

	static void main(String[] args) throws IOException {
		BufferedReader input = System.in.newReader()
		println("\t\tCliente Socket\n" +
				"1. TCP.\n" +
				"2. UDP.\n" +
				"0. Sair.")
		int protocolo = input.readLine().toInteger()
		while (protocolo != 0) {
			switch (protocolo) {
				case 1:
					clientTCP(input)
					break
				case 2:
					clientUDP(input)
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

	static void clientTCP(final BufferedReader input = System.in.newReader()) throws IOException {
		println("1. Receber arquivo do Servidor.\n" +
				"2. Enviar arquivo do Servidor.\n" +
				"0. Sair")
		int opcao = input.readLine().toInteger()

		while (opcao != 0) {
			if (opcao == 1) {
				int byteLido
				int byteAtual = 0
				FileOutputStream fileOutputStream = null
				BufferedOutputStream bufferedOutputStream = null
				String nomeArquivo = ""

				try {
					final boolean sucesso = receberBufferTCP { final Socket socket ->
						ObjectInputStream entrada = new ObjectInputStream(socket.getInputStream())
						nomeArquivo = entrada.readUTF()
					}

					if (!nomeArquivo || !sucesso) {
						println("1. Receber arquivo do Servidor.\n" +
								"2. Enviar arquivo ao Servidor.\n" +
								"0. Sair")
						opcao = input.readLine().toInteger()
						continue
					}

					receberBufferTCP { final Socket socket ->
						byte[] bytes = new byte[FILE_SIZE]
						InputStream inputStream = socket.getInputStream()
						Files.write(Paths.get("$DESKTOP_PATH$nomeArquivo"), new byte[0])
						bufferedOutputStream = new BufferedOutputStream(new FileOutputStream("$DESKTOP_PATH$nomeArquivo"))
						byteLido = inputStream.read(bytes, byteAtual, (bytes.length - byteAtual))
						while (byteLido > -1) {
							byteAtual += byteLido
							byteLido = inputStream.read(bytes, byteAtual, (bytes.length - byteAtual))
						}

						bufferedOutputStream.write(bytes, 0, byteAtual)
						bufferedOutputStream.flush()
						bufferedOutputStream.close()
						inputStream.close()
						println("Arquivo $nomeArquivo baixado ($byteAtual bytes lidos) será salvo na área de trabalho")
					}
				} finally {
					if (fileOutputStream) {
						fileOutputStream.close()
					}
					if (bufferedOutputStream) {
						bufferedOutputStream.close()
					}
				}
			} else if (opcao == 2) {
				BufferedInputStream inputStream
				OutputStream outputStream
				String caminhoDoArquivo
				byte[] bytes
				try {
					println("Insira o caminho do arquivo (de até 5MB): ")
					caminhoDoArquivo = input.readLine().replaceAll(/([^(\\{1,2}).+]?)\\{1,2}([^(\\{1,2}).+]?)/, /$1\/$2/)
					File file = new File(caminhoDoArquivo)
					while (!file.exists() || file.length() > FILE_SIZE) {
						println("Arquivo inexistente ou tamanho maior que o limite.\n" +
								"Insira um caminho de um arquivo válido: ")
						caminhoDoArquivo = input.readLine()
						file = new File(caminhoDoArquivo)
					}

					enviarBufferTCP { final Socket socket ->
						// envia nome do arquivo
						ObjectOutputStream saida = new ObjectOutputStream(socket.getOutputStream())
						saida.flush()
						saida.writeUTF(caminhoDoArquivo.find(/[A-Za-z0-9_\-\.]+\.[A-Za-z0-9]+\u0024/))
						saida.close()
					}

					enviarBufferTCP { final Socket socket ->
						// envia arquivo
						bytes = new byte[(int) file.length()]
						FileInputStream fis = new FileInputStream(file)
						inputStream = new BufferedInputStream(fis)
						inputStream.read(bytes, 0, bytes.length)
						outputStream = socket.getOutputStream()
						println("Enviando $caminhoDoArquivo (${bytes.length} bytes)")
						outputStream.write(bytes, 0, bytes.length)
						outputStream.flush()
						inputStream.close()
						outputStream.close()
					}

				} catch (Exception ex) {
					System.err.println("Impossível enviar o arquivo.")
					ex.printStackTrace()
				} finally {
					if (inputStream) {
						inputStream.close()
					}
					if (outputStream) {
						outputStream.close()
					}
				}
			}
			println("1. Receber arquivo do Servidor.\n" +
					"2. Enviar arquivo do Servidor.\n" +
					"0. Sair")
			opcao = input.readLine().toInteger()
		}
	}

	static void clientUDP(final BufferedReader input = System.in.newReader()) throws IOException {
		boolean continuar = true
		DatagramSocket datagramSocket = new DatagramSocket(UDP_PORT)
		DatagramPacket datagramPacket

		while (continuar) {
			byte[] bytesNomeArquivo = new byte[FILE_SIZE_UDP]
			try {
				println("Aguardando...")
				datagramPacket = new DatagramPacket(bytesNomeArquivo, bytesNomeArquivo.length)
				datagramSocket.receive(datagramPacket)

				String nomeArquivo = bytesToString(bytesNomeArquivo)

				if (!nomeArquivo) {
					println("Continuar recebendo? (1 = sim, 0 = não)")
					continuar = input.readLine().toInteger() as boolean
					continue
				}

				byte[] bytes = new byte[FILE_SIZE]
				int tamanhoTotal = 0
				byte[] tamanhoDeByteAtual = new byte[FILE_SIZE_UDP]
				datagramPacket = new DatagramPacket(tamanhoDeByteAtual, tamanhoDeByteAtual.length)
				datagramSocket.receive(datagramPacket)
				int tamanho = Integer.valueOf(bytesToString(tamanhoDeByteAtual))

				while (tamanho > FILE_SIZE_UDP) {
					datagramPacket = new DatagramPacket(bytes, tamanho)
					datagramSocket.receive(datagramPacket)
					tamanhoTotal += tamanho
					tamanhoDeByteAtual = new byte[FILE_SIZE_UDP]
					datagramPacket = new DatagramPacket(tamanhoDeByteAtual, tamanhoDeByteAtual.length)
					datagramSocket.receive(datagramPacket)
					tamanho = Integer.valueOf(bytesToString(tamanhoDeByteAtual))
				}

				bytes = Arrays.copyOfRange(bytes, 0, tamanhoTotal)

				Files.write(Paths.get("$DESKTOP_PATH$nomeArquivo"), bytes)
				println("Arquivo $nomeArquivo baixado (${bytes.length} bytes lidos) será salvo na área de trabalho")

				println("Continuar recebendo? (1 = sim, 0 = não)")
				continuar = input.readLine().toInteger() as boolean
			} catch (Exception ex) {
				System.err.println("Impossível estabelecer conexão com a porta do servidor.")
				ex.printStackTrace()
			}
		}
	}

	private static boolean receberBufferTCP(Closure closure) {
		try {
			println("Conectando...")
			final Socket socket = new Socket(SERVER, TCP_PORT)
			println("Conectado.")
			closure.call(socket)
			socket.close()
			return true
		} catch (Exception ex) {
			System.err.println("Impossível estabelecer conexão com a porta do servidor.")
			ex.printStackTrace()
			return false
		}
	}

	private static boolean enviarBufferTCP(Closure closure) {
		try {
			println("Conectando...")
			final Socket socket = new Socket(SERVER, TCP_PORT)
			println("Conectado.")
			closure.call(socket)
			socket.close()
			return true
		} catch (Exception ex) {
			System.err.println("Impossível estabelecer conexão com a porta do servidor.")
			ex.printStackTrace()
			return false
		}
	}

	static String bytesToString(byte[] a) {
		if (a == null) {
			return null
		}
		StringBuilder ret = new StringBuilder()
		for (int i = 0; a[i] != 0 as byte; i++) {
			ret.append((char) a[i])
		}
		return ret.toString()
	}
}
