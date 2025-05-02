package io.twentysixty.langchain.hologram.chatbot.svc;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.graalvm.collections.Pair;
import org.jboss.logging.Logger;
import org.jgroups.util.Base64;

import com.fasterxml.jackson.core.JsonProcessingException;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.data.message.SystemMessage;
import io.twentysixty.langchain.hologram.chatbot.jms.MtProducer;
import io.twentysixty.langchain.hologram.chatbot.model.Memory;
import io.twentysixty.langchain.hologram.chatbot.model.Session;
import io.twentysixty.langchain.hologram.chatbot.res.c.MediaResource;
import io.twentysixty.sa.client.model.credential.CredentialType;
import io.twentysixty.sa.client.model.message.BaseMessage;
import io.twentysixty.sa.client.model.message.Claim;
import io.twentysixty.sa.client.model.message.ContextualMenuItem;
import io.twentysixty.sa.client.model.message.ContextualMenuSelect;
import io.twentysixty.sa.client.model.message.ContextualMenuUpdate;
import io.twentysixty.sa.client.model.message.IdentityProofRequestMessage;
import io.twentysixty.sa.client.model.message.IdentityProofSubmitMessage;
import io.twentysixty.sa.client.model.message.InvitationMessage;
import io.twentysixty.sa.client.model.message.MediaItem;
import io.twentysixty.sa.client.model.message.MediaMessage;
import io.twentysixty.sa.client.model.message.MenuDisplayMessage;
import io.twentysixty.sa.client.model.message.MenuItem;
import io.twentysixty.sa.client.model.message.MenuSelectMessage;
import io.twentysixty.sa.client.model.message.RequestedProofItem;
import io.twentysixty.sa.client.model.message.SubmitProofItem;
import io.twentysixty.sa.client.model.message.TextMessage;
import io.twentysixty.sa.client.util.JsonUtil;
import io.twentysixty.sa.res.c.CredentialTypeResource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;



@ApplicationScoped
public class Service {

	private static Logger logger = Logger.getLogger(Service.class);

	@Inject EntityManager em;
	@Inject AnimatorService animService;
	
	@RestClient
	@Inject MediaResource mediaResource;
	
	
	@Inject MtProducer mtProducer;
	
	@Inject ChatBot bot;

	
	@RestClient
	@Inject CredentialTypeResource credentialTypeResource;
	
	
	@ConfigProperty(name = "io.twentysixty.demos.auth.debug")
	Boolean debug;
	
	
	@ConfigProperty(name = "io.twentysixty.demos.auth.credential_issuer")
	String credentialIssuer;
	
	@ConfigProperty(name = "io.twentysixty.demos.auth.credential_issuer.avatar")
	String invitationImageUrl;
	
	@ConfigProperty(name = "io.twentysixty.demos.auth.credential_issuer.label")
	String invitationLabel;
	
	@ConfigProperty(name = "io.twentysixty.demos.auth.enabled")
	Boolean authEnabled;
	
	
	@ConfigProperty(name = "io.twentysixty.demos.auth.id_credential_def")
	String credDef;
	
	@ConfigProperty(name = "io.twentysixty.demos.auth.messages.welcome")
	String WELCOME;
	
	@ConfigProperty(name = "io.twentysixty.demos.auth.messages.welcome2")
	Optional<String> WELCOME2;

	@ConfigProperty(name = "io.twentysixty.demos.auth.messages.welcome3")
	Optional<String> WELCOME3;

	@ConfigProperty(name = "io.twentysixty.demos.auth.messages.auth_success")
	Optional<String> AUTH_SUCCESS;

	
	@ConfigProperty(name = "io.twentysixty.demos.auth.messages.nocred")
	String NO_CRED_MSG;

	@ConfigProperty(name = "io.twentysixty.demos.auth.request.citizenid")
	Boolean requestCitizenId;

	
	@ConfigProperty(name = "io.twentysixty.demos.auth.request.firstname")
	Boolean requestFirstname;

	@ConfigProperty(name = "io.twentysixty.demos.auth.request.lastname")
	Boolean requestLastname;

	@ConfigProperty(name = "io.twentysixty.demos.auth.request.photo")
	Boolean requestPhoto;

	@ConfigProperty(name = "io.twentysixty.demos.auth.request.avatarname")
	Boolean requestAvatarname;

		
	@ConfigProperty(name = "io.twentysixty.demos.auth.language")
	Optional<String> language;

	@ConfigProperty(name = "io.twentysixty.demos.auth.vision.face.verification.url")
	String faceVerificationUrl;
	
	@ConfigProperty(name = "io.twentysixty.demos.auth.vision.redirdomain")
	Optional<String> redirDomain;
	
	@ConfigProperty(name = "io.twentysixty.demos.auth.vision.redirdomain.q")
	Optional<String> qRedirDomain;
	
	@ConfigProperty(name = "io.twentysixty.demos.auth.vision.redirdomain.d")
	Optional<String> dRedirDomain;
	
	@ConfigProperty(name = "io.twentysixty.langchain.hologram.chatbot.anim.random.commands")
	Optional<String> changeCommands;
	
	
	
	private static String[] models = null;
	private static String defaultModel = null;
	
	
	private static String CMD_ROOT_MENU_AUTHENTICATE = "/auth";
	private static String CMD_ROOT_MENU_NO_CRED = "/nocred";
	private static String CMD_ROOT_MENU_OPTION1 = "/option1";
	private static String CMD_ROOT_MENU_LOGOUT = "/logout";
	
	private static String CMD_ROOT_MENU_HELP = "/help";
	private static String CMD_ROOT_MENU_ANIMATOR = "/anim";
	private static String CMD_ROOT_MENU_RANDOM = "/random";
	
	private static String CMD_ROOT_MENU_SET_MODEL = "/set";
	
	private static String CMD_ROOT_MENU_CLEAR = "/clear";
	
	private Random random = new Random();
	
	
	@ConfigProperty(name = "io.twentysixty.demos.auth.messages.root.menu.title")
	String ROOT_MENU_TITLE;
	
	
	@ConfigProperty(name = "io.twentysixty.demos.auth.messages.root.menu.option1")
	String ROOT_MENU_OPTION1;
	
	@ConfigProperty(name = "io.twentysixty.demos.auth.messages.root.menu.no_cred")
	Optional<String> ROOT_MENU_NO_CRED;
	
	
	
	@ConfigProperty(name = "io.twentysixty.demos.auth.messages.option1")
	String OPTION1_MSG;
	
	
	
	
	
	
	//private static HashMap<UUID, SessionData> sessions = new HashMap<UUID, SessionData>();
	private static CredentialType type = null;
	private static Object lockObj = new Object();
	
	
	
	
	
	
	public void newConnection(UUID connectionId) throws Exception {
		UUID threadId = UUID.randomUUID();
		mtProducer.sendMessage(TextMessage.build(connectionId,threadId , WELCOME));
		if (WELCOME2.isPresent()) {
			mtProducer.sendMessage(TextMessage.build(connectionId, threadId, WELCOME2.get()));
		}
		if (WELCOME3.isPresent()) {
			mtProducer.sendMessage(TextMessage.build(connectionId, threadId, WELCOME3.get()));
		}
		
		
		mtProducer.sendMessage(this.getRootMenu(connectionId, null));
		
		if (authEnabled) {
			mtProducer.sendMessage(this.getIdentityCredentialRequest(connectionId, null));
			
		} else {
			mtProducer.sendMessage(this.getAnimatorMenu(connectionId, null));
		}
		//entryPointCreate(connectionId, null, null);
	}
	

	
	private BaseMessage getIdentityCredentialRequest(UUID connectionId, UUID threadId) {
		IdentityProofRequestMessage ip = new IdentityProofRequestMessage();
		ip.setConnectionId(connectionId);
		ip.setThreadId(threadId);
		
		RequestedProofItem id = new RequestedProofItem();
		id.setCredentialDefinitionId(credDef);
		id.setType("verifiable-credential");
		List<String> attributes = new ArrayList<String>();
		if (requestCitizenId) attributes.add("citizenId");
		if (requestFirstname) attributes.add("firstname");
		if (requestLastname) attributes.add("lastname");
		if (requestPhoto) attributes.add("photo");
		if (requestAvatarname) attributes.add("avatarName");
		attributes.add("issued");
		id.setAttributes(attributes);
		
		List<RequestedProofItem> rpi = new ArrayList<RequestedProofItem>();
		rpi.add(id);
		
		ip.setRequestedProofItems(rpi);
	
		
		try {
			logger.info("getCredentialRequest: claim: " + JsonUtil.serialize(ip, false));
		} catch (Exception e) {
			
		}
		return ip;
	}
	
	private Session getSession(UUID connectionId) {
		Session session = em.find(Session.class, connectionId);
		if (session == null) {
			session = new Session();
			session.setConnectionId(connectionId);
			em.persist(session);
			
			
		}
		
		return session;
	}
	
	
	
	Pair<String, byte[]> getImage(String image) {
		String mimeType = null;
		byte[] imageBytes = null;
		
		String[] separated =  image.split(";");
		if (separated.length>1) {
			String[] mimeTypeData = separated[0].split(":");
			String[] imageData = separated[1].split(",");
			
			if (mimeTypeData.length>1) {
				mimeType = mimeTypeData[1];
			}
			if (imageData.length>1) {
				String base64Image = imageData[1];
				if (base64Image != null) {
					try {
						imageBytes = Base64.decode(base64Image);
					} catch (IOException e) {
						logger.error("", e);
					}
				}
			}
			
		}
		
		if (mimeType == null) return null;
		if (imageBytes == null) return null;
		
		return Pair.create(mimeType, imageBytes);
		
	}
	ResourceBundle bundle = null; 
	
	private String getMessage(String messageName) {
		String retval = messageName;
		if (bundle == null) {
			if (language.isPresent()) {
				try {
					bundle = ResourceBundle.getBundle("META-INF/resources/Messages", new Locale(language.get())); 
				} catch (Exception e) {
					bundle = ResourceBundle.getBundle("META-INF/resources/Messages", new Locale("en")); 
				}
			} else {
				bundle = ResourceBundle.getBundle("META-INF/resources/Messages", new Locale("en")); 
			}
			
		}
		try {
			retval = bundle.getString(messageName);
		} catch (Exception e) {
			
		}
		
		
		return retval;
	}
	
	private String buildVisionUrl(String url) {
		
		if(redirDomain.isPresent()) {
			url = url + "&rd=" +  redirDomain.get();
		}
		if(qRedirDomain.isPresent()) {
			url = url + "&q=" +  qRedirDomain.get();
		}
		if(dRedirDomain.isPresent()) {
			url = url + "&d=" +  dRedirDomain.get();
		}
		if (language.isPresent()) {
			url = url + "&lang=" +  language.get();
		}
		
		return url;
	}
	private BaseMessage generateFaceVerificationMediaMessage(UUID connectionId, UUID threadId, String token) {
		String url = faceVerificationUrl.replaceFirst("TOKEN", token);
		url = this.buildVisionUrl(url);
		
		MediaItem mi = new MediaItem();
		mi.setMimeType("text/html");
		mi.setUri(url);
		mi.setTitle(getMessage("FACE_VERIFICATION_HEADER"));
		mi.setDescription(getMessage("FACE_VERIFICATION_DESC"));
		mi.setOpeningMode("normal");
		List<MediaItem> mis = new ArrayList<MediaItem>();
		mis.add(mi);
		MediaMessage mm = new MediaMessage();
		mm.setConnectionId(connectionId);
		mm.setThreadId(threadId);
		mm.setDescription(getMessage("FACE_VERIFICATION_DESC"));
		mm.setItems(mis);
		return mm;
	}
	
	private boolean changeCommand(String content) {
		
		if (content.startsWith("/")) {
			if (changeCommands.isPresent()) {
				if (changeCommands.get().length()>0) {
					String[] commands = changeCommands.get().split(",");
					for (int i=0; i<commands.length; i++) {
						String cur = commands[i];
						if (cur!=null) {
							cur = cur.strip();
							
							if (content.equals("/" + cur)) {
								return true;
							}
						}
					}
				}
			}
		}
		return false;
	}
	
	@Transactional
	public void userInput(BaseMessage message) throws Exception {
		
		Session session = this.getSession(message.getConnectionId());
		
		
		
		String content = null;
		boolean contextual = false;
		MediaMessage mm = null;
		
		if (message instanceof TextMessage) {
			
			TextMessage textMessage = (TextMessage) message;
			content = textMessage.getContent();

		} else if ((message instanceof ContextualMenuSelect) ) {
			
			ContextualMenuSelect menuSelect = (ContextualMenuSelect) message;
			content = menuSelect.getSelectionId();
			contextual = true;
		} else if ((message instanceof MenuSelectMessage)) {
			
			MenuSelectMessage menuSelect = (MenuSelectMessage) message;
			content = menuSelect.getMenuItems().iterator().next().getId();
		} else if ((message instanceof MediaMessage)) {
			mm = (MediaMessage) message;
			content = "media";
		} else if ((message instanceof IdentityProofSubmitMessage)) {
			if (session.getAuthTs() == null) {
				try {
					logger.info("userInput: claim: " + JsonUtil.serialize(message, false));
				} catch (JsonProcessingException e) {
					
				}
				boolean sentVerifLink = false;
				boolean credentialReceived = false;
				IdentityProofSubmitMessage ipm = (IdentityProofSubmitMessage) message;
				
				if (ipm.getSubmittedProofItems().size()>0) {
					
					SubmitProofItem sp = ipm.getSubmittedProofItems().iterator().next();
					
					if ((sp.getVerified() != null) && (sp.getVerified())) {
						if (sp.getClaims().size()>0) {
							
							String citizenId = null;
							String firstname = null;
							String lastname = null;
							String photo = null;
							String avatarName = null;
							
							for (Claim c: sp.getClaims()) {
								if (c.getName().equals("citizenId")) {
									citizenId = c.getValue();
								} else if (c.getName().equals("firstname")) {
									firstname = c.getValue();
								} else if (c.getName().equals("lastname")) {
									lastname = c.getValue();
								} else if (c.getName().equals("photo")) {
									photo = c.getValue();
									logger.info("userInput: photo: " + photo);
								} else if (c.getName().equals("avatarName")) {
									avatarName = c.getValue();
								} 
							}
							session.setCitizenId(citizenId);
							session.setFirstname(firstname);
							session.setLastname(lastname);
							session.setAvatarName(avatarName);
							
							/*if (photo != null) {
								Pair<String, byte[]> imageData = getImage(photo);
								if (imageData != null) {
									UUID mediaUUID = UUID.randomUUID();
									mediaResource.createOrUpdate(mediaUUID, 1, mediaUUID.toString());
									
									
									File file = new File(System.getProperty("java.io.tmpdir") + "/" + mediaUUID);
									
									FileOutputStream fos = new FileOutputStream(file);
									fos.write(imageData.getRight());
									fos.flush();
									fos.close();
									
									Resource r = new Resource();
									r.chunk = new FileInputStream(file);
									mediaResource.uploadChunk(mediaUUID, 0, mediaUUID.toString(), r);
									
									file.delete();
									session.setPhoto(mediaUUID);
									session.setToken(UUID.randomUUID());
									em.merge(session);
									
									mtProducer.sendMessage(generateFaceVerificationMediaMessage(message.getConnectionId(), message.getThreadId(), session.getToken().toString()));
									
									sentVerifLink = true;
								}
							}*/
							
							
						}
						credentialReceived = true;
					}
					}
					
				if (credentialReceived) {
					if (!sentVerifLink) {
						
						notifySuccess(session.getConnectionId());
						
						//mtProducer.sendMessage(TextMessage.build(message.getConnectionId(), message.getThreadId() , this.getMessage("CREDENTIAL_ERROR")));

					}
				} else {
					mtProducer.sendMessage(TextMessage.build(message.getConnectionId(), message.getThreadId() , NO_CRED_MSG));
					mtProducer.sendMessage(this.getInvitationMessage(message.getConnectionId(), message.getThreadId()));
					
				}
				
			}
			
		}
		if (content != null) {
			content = content.strip();
					
			if (content.equals(CMD_ROOT_MENU_AUTHENTICATE.toString())) {
				mtProducer.sendMessage(this.getIdentityCredentialRequest(message.getConnectionId(), message.getThreadId()));
			} else if (content.equals(CMD_ROOT_MENU_OPTION1.toString())) {
				mtProducer.sendMessage(TextMessage.build(message.getConnectionId(), message.getThreadId() , OPTION1_MSG));

			} else if (content.equals(CMD_ROOT_MENU_NO_CRED.toString())) {
				
				mtProducer.sendMessage(TextMessage.build(message.getConnectionId(), message.getThreadId() , NO_CRED_MSG));
				mtProducer.sendMessage(this.getInvitationMessage(message.getConnectionId(), message.getThreadId()));

			} else if (content.equals(CMD_ROOT_MENU_LOGOUT.toString())) {
				if (session != null) {
					session.setAuthTs(null);
					session = em.merge(session);
				}
				mtProducer.sendMessage(TextMessage.build(message.getConnectionId(), message.getThreadId() , this.getMessage("UNAUTHENTICATED")));

			} else if (content.equals(CMD_ROOT_MENU_HELP.toString())) {
				
				mtProducer.sendMessage(TextMessage.build(message.getConnectionId(), message.getThreadId() , this.getMessage("USAGE")));

			} else if (content.equals(CMD_ROOT_MENU_CLEAR.toString())) {
				
				if (session != null) {
					
					if (session.getMemory() != null) {
						this.resetMemory(session.getMemory().getMemoryId());
					}
					
				}
				
				mtProducer.sendMessage(TextMessage.build(message.getConnectionId(), message.getThreadId() , this.getMessage("CLEANED_HISTORY")));

			}  else if ((session != null) && (session.getAuthTs() != null)) {
				
				if (content.startsWith(CMD_ROOT_MENU_ANIMATOR.toString())) {
					
					String[] specifiedAnim = content.split(" ");
					if (specifiedAnim.length == 1) {
						mtProducer.sendMessage(this.getAnimatorMenu(message.getConnectionId(), message.getThreadId()));
					} else {
						Integer anim = null;
						try {
							anim = Integer.parseInt(specifiedAnim[1]);
							this.setAnimator(message.getConnectionId(), session, anim);
							mtProducer.sendMessage(TextMessage.build(message.getConnectionId(), message.getThreadId(), this.getMessage("ANIM_SELECTED").replaceAll("ANIMATOR", getAnimatorLabel(session))));

							String result = bot.chat(session.getMemory().getMemoryId(), animService.get(anim).getHello());
							mtProducer.sendMessage(TextMessage.build(message.getConnectionId(), message.getThreadId(), result));
						} catch (Exception e) {
							
						}
					}
					

				} else if (content.startsWith(CMD_ROOT_MENU_RANDOM.toString()) || this.changeCommand(content)) {
					
					
					TreeMap<Integer,Animator> anims = animService.getAnimators();
					
					if (anims.size() == 1) {
						this.setAnimator(message.getConnectionId(), session, 0);
						mtProducer.sendMessage(TextMessage.build(message.getConnectionId(), message.getThreadId(), this.getMessage("ANIM_SELECTED").replaceAll("ANIMATOR", getAnimatorLabel(session))));

						String result = bot.chat(session.getMemory().getMemoryId(), animService.get(0).getHello());
						mtProducer.sendMessage(TextMessage.build(message.getConnectionId(), message.getThreadId(), result));
					} else {
						Set<Integer> keys = anims.keySet();
						List<Integer> others = new ArrayList<Integer>();
						Integer currentAnim = null;
						if (session.getMemory() != null) {
							currentAnim = session.getMemory().getAnimator();
						}
						for (Integer current: keys) {
							if (currentAnim != null) {
								if (current.intValue() != currentAnim.intValue()) {
									others.add(current);
								}
							} else {
								others.add(current);
							}
						}
						
						Integer newAnim = others.get(random.nextInt(others.size()));
						this.setAnimator(message.getConnectionId(), session, newAnim);
						mtProducer.sendMessage(TextMessage.build(message.getConnectionId(), message.getThreadId(), this.getMessage("ANIM_SELECTED").replaceAll("ANIMATOR", getAnimatorLabel(session))));

						String result = bot.chat(session.getMemory().getMemoryId(), animService.get(newAnim).getHello());
						mtProducer.sendMessage(TextMessage.build(message.getConnectionId(), message.getThreadId(), result));
					}
					
					

				} else {
					if (session.getMemory() == null) {
						mtProducer.sendMessage(this.getAnimatorMenu(message.getConnectionId(), message.getThreadId()));
					} else {
						
						String result = bot.chat(session.getMemory().getMemoryId(), content);
						mtProducer.sendMessage(TextMessage.build(message.getConnectionId(), message.getThreadId(), result));
					}
				}
				
				
				
			} else {
				mtProducer.sendMessage(TextMessage.build(message.getConnectionId(), message.getThreadId() , this.getMessage("ERROR_NOT_AUTHENTICATED")));
			}
			
			
		}
		mtProducer.sendMessage(this.getRootMenu(message.getConnectionId(), session));
	}
	
	
	private BaseMessage getInvitationMessage(UUID connectionId, UUID threadId) {
		InvitationMessage invitation = new InvitationMessage();
		invitation.setConnectionId(connectionId);
		invitation.setThreadId(threadId);
		invitation.setImageUrl(invitationImageUrl);
		invitation.setDid(credentialIssuer);
		invitation.setLabel(invitationLabel);
		return invitation;
	}

	
	
	@Transactional
	public void notifySuccess(UUID connectionId) {
		Session session = em.find(Session.class, connectionId);
		if (session != null) {
			try {
				session.setAuthTs(Instant.now());
				session = em.merge(session);
				if (AUTH_SUCCESS.isPresent()) {
					mtProducer.sendMessage(TextMessage.build(connectionId, null , AUTH_SUCCESS.get()));
				} else {
					mtProducer.sendMessage(TextMessage.build(connectionId, null , this.getMessage("AUTHENTICATION_SUCCESS")));
				}
				//mtProducer.sendMessage(TextMessage.build(connectionId, null , this.getMessage("SET_MODEL").replaceAll("MODEL", session.getModel())));
				mtProducer.sendMessage(this.getAnimatorMenu(connectionId, null));
			} catch (Exception e) {
				logger.error("", e);
			}
		}
		try {
			mtProducer.sendMessage(this.getRootMenu(connectionId, session));
		} catch (Exception e) {
			logger.error("", e);
		}
	}

	public void notifyFailure(UUID connectionId) {
		Session session = em.find(Session.class, connectionId);
		if (session != null) {
			try {
				mtProducer.sendMessage(TextMessage.build(connectionId, null , this.getMessage("AUTHENTICATION_ERROR")));
				
				mtProducer.sendMessage(generateFaceVerificationMediaMessage(connectionId, null, session.getToken().toString()));
			} catch (Exception e) {
				logger.error("", e);
			}
		}
	}
	
	
	public BaseMessage getAnimatorMenu(UUID connectionId, UUID threadId) {
		List<MenuItem> menuItems = new ArrayList<MenuItem>();
		
		MenuDisplayMessage confirm = new MenuDisplayMessage();
		confirm.setPrompt(getMessage("CHOOSE_AN_ANIMATOR"));
		
		TreeMap<Integer,Animator> anims = animService.getAnimators();
		
		for (Integer ak: anims.keySet()) {
			Animator a = anims.get(ak);
			MenuItem am = new MenuItem();
			am.setId(CMD_ROOT_MENU_ANIMATOR + " " + ak);
			am.setText(a.getName() + ", " + a.getAge() + ", " + a.getPlace() + " (" + a.getLanguage() + ")");
			menuItems.add(am);
		}
		
		confirm.setConnectionId(connectionId);
		confirm.setThreadId(threadId);
		confirm.setMenuItems(menuItems);
		return confirm;
	}
	 
	@Transactional
	public UUID setAnimator(UUID connectionId, Session session, int animator) {
		
		Query q = em.createNamedQuery("Session.findMemory");
		q.setParameter("connectionId", connectionId);
		q.setParameter("animator", animator);
		Memory memory = (Memory) q.getResultList().stream().findFirst().orElse(null);
		if (memory == null) {
			memory = new Memory();
			memory.setConnectionId(connectionId);
			memory.setMemoryId(UUID.randomUUID());
			memory.setAnimator(animator);
			em.persist(memory);
			this.resetMemory(memory.getMemoryId());
		}
		if ((session.getMemory() == null) || (!session.getMemory().getMemoryId().equals(memory.getMemoryId()))) {
			session.setMemory(memory);
			session = em.merge(session);
		}
		return memory.getMemoryId();
		
	}
	
	@Transactional
	public void resetMemory(UUID memoryId) {
		Memory memory = em.find(Memory.class, memoryId);
		
		if (memory == null) return;
		
		Animator anim = animService.get(memory.getAnimator());
		List<ChatMessage> messages = new ArrayList<ChatMessage>(0);
		ChatMessage m = SystemMessage.systemMessage(anim.getPrompt());
		messages.add(m);
		memory.setMemory(ChatMessageSerializer.messagesToJson(messages));
		memory = em.merge(memory);
	}
	
	
	private String getAnimatorLabel(Session session) {
		if (session.getMemory() != null) {
			Animator a = animService.get(session.getMemory().getAnimator());
			return a.getSummary();
		} else {
			return null;
		}
	}
	
	public BaseMessage getRootMenu(UUID connectionId, Session session) {
		
		ContextualMenuUpdate menu = new ContextualMenuUpdate();
		menu.setTitle(ROOT_MENU_TITLE);
		
		
		List<ContextualMenuItem> options = new ArrayList<ContextualMenuItem>();
		
		
		if ((session == null) || (( authEnabled) && (session.getAuthTs() == null)  )){
			menu.setDescription(getMessage("ROOT_MENU_DEFAULT_DESCRIPTION"));
			options.add(ContextualMenuItem.build(CMD_ROOT_MENU_AUTHENTICATE, getMessage("ROOT_MENU_AUTHENTICATE"), null));
			if (ROOT_MENU_NO_CRED.isPresent()) {
				options.add(ContextualMenuItem.build(CMD_ROOT_MENU_NO_CRED, ROOT_MENU_NO_CRED.get(), null));
			} else {
				options.add(ContextualMenuItem.build(CMD_ROOT_MENU_NO_CRED, getMessage("ROOT_MENU_NO_CRED"), null));
			}
			
			
		} else {
			
			if ((session.getFirstname() != null) && (session.getLastname() != null)) {
				String animLabel = this.getAnimatorLabel(session);
				if (animLabel != null) {
					menu.setDescription(getMessage("ROOT_MENU_AUTHENTICATED_DESCRIPTION_ANIM_SELECTED").replaceAll("NAME", session.getFirstname() + " " + session.getLastname()).replaceAll("ANIMATOR", getAnimatorLabel(session)));

				} else {
					menu.setDescription(getMessage("ROOT_MENU_AUTHENTICATED_DESCRIPTION").replaceAll("NAME", session.getFirstname() + " " + session.getLastname()));

				}
				
			} else if (session.getAvatarName() != null){
				
				
				String animLabel = this.getAnimatorLabel(session);
				if (animLabel != null) {
					menu.setDescription(getMessage("ROOT_MENU_AUTHENTICATED_DESCRIPTION_ANIM_SELECTED").replaceAll("NAME", session.getAvatarName()).replaceAll("ANIMATOR", getAnimatorLabel(session)));

				} else {
					menu.setDescription(getMessage("ROOT_MENU_AUTHENTICATED_DESCRIPTION").replaceAll("NAME", session.getAvatarName()));

				}
				
				
				
			} else if (session.getCitizenId() != null) {
				
				String animLabel = this.getAnimatorLabel(session);
				if (animLabel != null) {
					menu.setDescription(getMessage("ROOT_MENU_AUTHENTICATED_DESCRIPTION_ANIM_SELECTED").replaceAll("NAME", session.getCitizenId()).replaceAll("ANIMATOR", getAnimatorLabel(session)));

				} else {
					menu.setDescription(getMessage("ROOT_MENU_AUTHENTICATED_DESCRIPTION").replaceAll("NAME", session.getCitizenId()));

				}
			} 
			
			options.add(ContextualMenuItem.build(CMD_ROOT_MENU_HELP, this.getMessage("ROOT_MENU_HELP"), null));
			options.add(ContextualMenuItem.build(CMD_ROOT_MENU_ANIMATOR, this.getMessage("ROOT_MENU_ANIMS"), null));
			
			if (authEnabled) {
				options.add(ContextualMenuItem.build(CMD_ROOT_MENU_LOGOUT, this.getMessage("ROOT_MENU_LOGOUT"), null));
				
			}
			
		}
		
		
		
		menu.setOptions(options);
		


		if (debug) {
			try {
				logger.info("getRootMenu: " + JsonUtil.serialize(menu, false));
			} catch (JsonProcessingException e) {
			}
		}
		menu.setConnectionId(connectionId);
		menu.setId(UUID.randomUUID());
		menu.setTimestamp(Instant.now());
		
		return menu;
		

	}
}
